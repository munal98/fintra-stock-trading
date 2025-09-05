const authConfig = {
    loginEndpoint: 'http://localhost:8080/api/v1/auth/login',
    storageTokenKeyName: 'accessToken',
    storageEncryptedKey: 'encKey',
    onTokenExpiration: 'refreshToken',
    key: '-----BEGIN PUBLIC KEY-----MIIBITANBgkqhkiG9w0BAQEFAAOCAQ4AMIIBCQKCAQBqpp4zL7qhHE+ywv4M76r4+dFA2nwZGYKMpl3EmT8LHsYARWz07u9dM+7QUNtCRkc6yVy64UZC8jd5kHgnvFdI5+l7IJ0WiSjG7qp40QoT2qtp75S2kDCuKCNT59YwvRPLd7hFL7pock7hfpw9k4w0R3PR29rpvpSq7lGuSgnQTnEVcyt2xIoEVYlYfs/+mzBSOWqrEWDq7YLXCnoCz0vRZwxrzyCTxu3xd+Ij1WdlXBpTk0oty76M3xE+aZO07aGugD1Dwy/cHjx5/Eo0/AoxzxT2lY2SUXkXqcSlCI0BRuRYW9PR7YESRszdBGzEPgjSmvDzESg2DrXnVy7QNxAbAgMBAAE=-----END PUBLIC KEY-----'
};

export default authConfig;
