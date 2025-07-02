from hashlib import sha256 as hashlib_sha256
from hmac import compare_digest as hmac_compare_digest
from hmac import new as hmac_new
from struct import error as struct_error
from struct import pack as struct_pack
from struct import unpack as struct_unpack
from uuid import UUID

from cryptography.hazmat.backends import default_backend
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.ciphers import Cipher
from cryptography.hazmat.primitives.ciphers import algorithms
from cryptography.hazmat.primitives.ciphers import modes
from cryptography.hazmat.primitives.kdf.pbkdf2 import PBKDF2HMAC

from src.core.config import settings
from src.core.log import BaseClass
from src.core.log import log_function_calls
from src.core.log import logger


class UUIDGeneratorError(Exception):
    pass


class UUIDGenerator(BaseClass):
    MAX_ID = 2**32 - 1
    MIN_SECRET_KEY_LENGTH = 16
    BLOCK_SIZE = 16
    UUID_LENGTH = 16

    def __init__(self, secret_key: str, salt: bytes):
        """Initialize UUID generator.

        Args:
            secret_key (str): Master secret for key derivation.
            salt (bytes): Salt for PBKDF2.

        Raises:
            UUIDGeneratorError: If secret key is too weak
        """
        self._SERVICE_NAME = "uuid_generator"

        if not secret_key or len(secret_key) < self.MIN_SECRET_KEY_LENGTH:
            raise UUIDGeneratorError(
                f"Secret key must be at least {self.MIN_SECRET_KEY_LENGTH} characters",
            )

        kdf = PBKDF2HMAC(
            algorithm=hashes.SHA256(),
            length=32,
            salt=salt,
            iterations=50000,
            backend=default_backend(),
        )
        self._key = kdf.derive(secret_key.encode("utf-8"))

        self._iv = hashlib_sha256(f"uuid_iv_{secret_key}".encode()).digest()[:16]

        with self.get_log_context("init"):
            logger.debug(
                "UUIDGenerator initialized with key and IV derived from secret key",
            )

    @log_function_calls(include_result=True)
    def int_to_uuid(self, id: int) -> str:
        """Convert integer ID to encrypted UUID.

        Args:
            id (int): ID (0 to 2^32-1)

        Returns:
            str: UUID string

        Raises:
            UUIDGeneratorError: If ID is invalid
        """
        # Validate input
        if not isinstance(id, int):  # type: ignore
            raise UUIDGeneratorError(f"ID must be integer, got {type(id).__name__}")

        if id < 0:
            raise UUIDGeneratorError("ID must be non-negative")

        if id > self.MAX_ID:
            raise UUIDGeneratorError(f"ID too large (max: {self.MAX_ID:,})")

        try:
            # Format: [4-byte ID][4-byte checksum][8-byte padding]
            id_bytes = struct_pack("<I", id)

            checksum = hmac_new(
                self._key[:16],
                id_bytes,
                hashlib_sha256,
            ).digest()[:4]

            padding = struct_pack("<II", id ^ 0xAAAAAAAA, (~id) & 0xFFFFFFFF)

            plaintext = id_bytes + checksum + padding

            cipher = Cipher(
                algorithms.AES(self._key),
                modes.CBC(self._iv),
                backend=default_backend(),
            )
            encryptor = cipher.encryptor()
            ciphertext = encryptor.update(plaintext) + encryptor.finalize()

            return str(UUID(bytes=ciphertext))

        except Exception as e:
            logger.error(f"UUID generation failed for ID {id}: {e}")
            raise UUIDGeneratorError(f"Failed to generate UUID: {e!s}") from e

    @log_function_calls(include_args=True)
    def uuid_to_int(self, uuid_str: str) -> int:
        """Convert UUID back to original ID.

        Args:
            uuid_str (str): UUID string

        Returns:
            int: Original user ID

        Raises:
            UUIDGeneratorError: If UUID is invalid
        """
        if not isinstance(uuid_str, str):  # type: ignore
            raise UUIDGeneratorError(
                f"UUID must be string, got {type(uuid_str).__name__}",
            )

        try:
            uuid_obj = UUID(uuid_str)
            ciphertext = uuid_obj.bytes
        except ValueError as e:
            raise UUIDGeneratorError(f"Invalid UUID format: {e}") from e

        if len(ciphertext) != self.UUID_LENGTH:
            raise UUIDGeneratorError("Invalid UUID length")

        try:
            cipher = Cipher(
                algorithms.AES(self._key),
                modes.CBC(self._iv),
                backend=default_backend(),
            )
            decryptor = cipher.decryptor()
            plaintext = decryptor.update(ciphertext) + decryptor.finalize()

            id = struct_unpack("<I", plaintext[:4])[0]
            stored_checksum = plaintext[4:8]

            expected_checksum = hmac_new(
                self._key[:16],
                plaintext[:4],
                hashlib_sha256,
            ).digest()[:4]

            if not hmac_compare_digest(stored_checksum, expected_checksum):
                raise UUIDGeneratorError("UUID integrity check failed")  # noqa: TRY301

            if id > self.MAX_ID:
                raise UUIDGeneratorError("Decrypted ID out of range")  # noqa: TRY301
        except struct_error as e:
            raise UUIDGeneratorError("UUID decryption failed - corrupt data") from e
        except Exception as e:
            raise UUIDGeneratorError("UUID decryption failed") from e
        else:
            return id

    @log_function_calls(include_args=True, include_result=True)
    def validate_uuid(self, uuid_str: str) -> bool:
        """Validate UUID and verify round-trip conversion.

        Args:
            uuid_str (str): UUID to validate
        Returns:
            bool: True if valid.
        """
        try:
            id = self.uuid_to_int(uuid_str)
            regenerated = self.int_to_uuid(id)
        except Exception:
            return False
        else:
            return regenerated == uuid_str


uuid_generator = UUIDGenerator(secret_key=settings.SECRET_KEY, salt=settings.SALT)
