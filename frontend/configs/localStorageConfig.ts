import crypto from 'crypto';

const key = new Uint8Array([
  234, 16, 110, 224, 119, 17, 68, 195, 216, 117, 47, 147,
  131, 104, 150, 43, 69, 136, 73, 253, 36, 79, 61, 28,
  68, 196, 175, 174, 226, 111, 198, 72
]);

const iv = new Uint8Array([
  106, 176, 189, 93, 54, 121, 216, 26,
  109, 54, 248, 95, 44, 8, 15, 250
]);

export const saveToLocalStorage = <T>(key: string, value: T): void => {
  try {
    const serializedValue = JSON.stringify(value);
    const encryptedValue = encrypt(serializedValue);
    localStorage.setItem(key, encryptedValue);
  } catch (e) {
    console.error('Local storage error (save):', e);
  }
};

export const getFromLocalStorage = <T>(key: string): T | undefined => {
  try {
    const encryptedValue = localStorage.getItem(key);
    if (!encryptedValue) return undefined;

    const decryptedValue = decrypt(encryptedValue);
    return JSON.parse(decryptedValue) as T;
  } catch (e) {
    console.error('Local storage error (get):', e);
    return undefined;
  }
};

const encrypt = (value: string): string => {
  const cipher = crypto.createCipheriv('aes-256-cbc', key, iv);
  let encrypted = cipher.update(value, 'utf8', 'hex');
  encrypted += cipher.final('hex');
  return encrypted;
};

const decrypt = (value: string): string => {
  const decipher = crypto.createDecipheriv('aes-256-cbc', key, iv);
  let decrypted = decipher.update(value, 'hex', 'utf8');
  decrypted += decipher.final('utf8');
  return decrypted;
};
