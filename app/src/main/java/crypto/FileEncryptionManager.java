package crypto;

import android.util.Log;

import java.io.File;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Map;

import utils.Base64Utils;
import utils.FileUtils;

import static crypto.RSAProvider.encryptPublicKey;

public class FileEncryptionManager {
    private static FileEncryptionManager INSTANCE;
    private String publicKey;
    private String privateKey;

    private FileEncryptionManager() {
    }

    public static FileEncryptionManager getInstance(){
        if (INSTANCE == null){
            INSTANCE = new FileEncryptionManager();
        }
        return INSTANCE;
    }

    /**
     * set the key of encrypt and decrypt
     * @param publicKey
     * @param privateKey
     * @param isEncode is encoded by Base64
     * @see #generateKey() ()
     */
    public void setRSAKey(String publicKey, String privateKey, boolean isEncode) throws Exception {
        if (isEncode){
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }else {
            this.publicKey = Base64Utils.encode(publicKey.getBytes());
            this.privateKey = Base64Utils.encode(privateKey.getBytes());
        }
    }

    /**
     * set the key of encrypt and decrypt
     * @param publicKey
     * @param privateKey
     * @see #generateKey() ()
     */
    public void setRSAKey(RSAPublicKey publicKey, RSAPrivateKey privateKey) throws Exception {
        this.publicKey = Base64Utils.encode(publicKey.getEncoded());
        this.privateKey = Base64Utils.encode(privateKey.getEncoded());
    }

    /**
     * generate public and private key
     * @throws Exception
     * @see #setRSAKey(RSAPublicKey, RSAPrivateKey)
     * @see #setRSAKey(String, String, boolean)
     */
    public void generateKey() throws Exception {
        Map<String, Object> map = RSAProvider.generateKeyPair();
        this.privateKey = RSAProvider.getPrivateKeyBytes(map);
        this.publicKey = RSAProvider.getPublicKeyBytes(map);
    }

    public String getPublicKey() {
        return publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    /**
     * sign data by private key
     * @param data the data by encrypted
     * @return
     * @throws Exception
     * @see #verifyByPublicKey(byte[], String)
     */
    public String signByPrivateKey(byte[] data) throws Exception {
        if (privateKey == null || privateKey.isEmpty()){
            throw new IllegalArgumentException("PrivateKey is empty, you should invoke setRSAKey or generateKey");
        }
        return RSAProvider.sign(data, privateKey);
    }

    /**
     * verify data by sign
     * @param data the data by encrypted
     * @param sign
     * @return
     * @throws Exception
     * @see #signByPrivateKey(byte[])
     */
    public boolean verifyByPublicKey(byte[] data, String sign) throws Exception {
        if (publicKey == null || publicKey.isEmpty()){
            throw new IllegalArgumentException("PublicKey is empty, you should invoke setRSAKey or generateKey");
        }
        return RSAProvider.verify(data, publicKey, sign);
    }

    /**
     * encrypt by public key
     * @param data
     * @return
     * @throws Exception
     */
    public byte[] encryptByPublicKey(byte[] data) throws Exception {
        if (publicKey == null || publicKey.isEmpty()){
            throw new IllegalArgumentException("PublicKey is empty, you should invoke setRSAKey or generateKey");
        }
        return encryptPublicKey(data, publicKey);
    }

    /**
     * decrypt by private key
     * @param data
     * @return
     * @throws Exception
     */
    public byte[] decryptByPrivateKey(byte[] data) throws Exception {
        if (privateKey == null || privateKey.isEmpty()){
            throw new IllegalArgumentException("PrivateKey is empty, you should invoke setRSAKey or generateKey");
        }
        return RSAProvider.decryptPrivateKey(data, privateKey);
    }

    /**
     * encrypt by private key
     * @param data
     * @return
     * @throws Exception
     */
    public byte[] encryptByPrivateKey(byte[] data) throws Exception {
        if (privateKey == null || privateKey.isEmpty()){
            throw new IllegalArgumentException("PrivateKey is empty, you should invoke setRSAKey or generateKey");
        }
        return encryptPublicKey(data, privateKey);
    }

    /**
     * decrypt by public key
     * @param data
     * @return
     * @throws Exception
     */
    public byte[] decryptByPublicKey(byte[] data) throws Exception {
        if (publicKey == null || publicKey.isEmpty()){
            throw new IllegalArgumentException("PublicKey is empty, you should invoke setRSAKey or generateKey");
        }
        return RSAProvider.decryptPrivateKey(data, publicKey);
    }

    /**
     * using public key encrypt file
     * @param inputFile the file of wait to encrypt
     * @param outFile
     * @return encrypted data
     */
    public byte[] encryptFileByPublicKey(File inputFile, File outFile) throws Exception {
        if (publicKey == null || publicKey.isEmpty()){
            throw new IllegalArgumentException("PublicKey is empty, you should invoke setRSAKey or generateKey");
        }
        byte[] data = FileUtils.getDataFromFile(inputFile);
        byte[] encryData = encryptPublicKey(data, publicKey);
        if (outFile != null){
            boolean result = FileUtils.saveDataToFile(encryData, outFile);
            Log.d("FileEncryptionManager", "save file result "+result);
        }
        return encryData;
    }

    /**
     * using private key decrypt file
     * @param inputFile the file is encrypted
     * @param outFile
     * @return origial data by decrypted
     */
    public byte[] decryptFileByPrivateKey(File inputFile, File outFile) throws Exception {
        if (privateKey == null || privateKey.isEmpty()){
            throw new IllegalArgumentException("PrivateKey is empty, you should invoke setRSAKey or generateKey");
        }
        byte[] data = FileUtils.getDataFromFile(inputFile);
        byte[] decryData = RSAProvider.decryptPrivateKey(data, privateKey);
        if (outFile != null){
            boolean result = FileUtils.saveDataToFile(decryData, outFile);
            Log.d("FileEncryptionManager", "save file result "+result);
        }
        return decryData;
    }

    /**
     * using public key encrypt file
     * @param inputData the file of wait to encrypt
     * @param outFile
     * @return encrypted data
     */
    public byte[] encryptFileByPublicKey(byte[] inputData, File outFile) throws Exception {
        if (publicKey == null || publicKey.isEmpty()){
            throw new IllegalArgumentException("PublicKey is empty, you should invoke setRSAKey or generateKey");
        }
        byte[] encryData = encryptPublicKey(inputData, publicKey);
        if (outFile != null){
            boolean result = FileUtils.saveDataToFile(encryData, outFile);
            Log.d("FileEncryptionManager", "save file result "+result);
        }
        return encryData;
    }

    /**
     * using private key decrypt file
     * @param inputData the file is encrypted
     * @param outFile
     * @return origial data by decrypted
     */
    public byte[] decryptFileByPrivateKey(byte[] inputData, File outFile) throws Exception {
        if (privateKey == null || privateKey.isEmpty()){
            throw new IllegalArgumentException("PrivateKey is empty, you should invoke setRSAKey or generateKey");
        }
        byte[] decryData = RSAProvider.decryptPrivateKey(inputData, privateKey);
        if (outFile != null){
            boolean result = FileUtils.saveDataToFile(decryData, outFile);
            Log.d("FileEncryptionManager", "save file result "+result);
        }
        return decryData;
    }

    /**
     * using private key encrypt file
     * @param inputFile the file of wait to encrypt
     * @param outFile
     * @return encrypted data
     */
    public byte[] encryptFileByPrivateKey(File inputFile, File outFile) throws Exception {
        if (privateKey == null || privateKey.isEmpty()){
            throw new IllegalArgumentException("PrivateKey is empty, you should invoke setRSAKey or generateKey");
        }
        byte[] data = FileUtils.getDataFromFile(inputFile);
        byte[] encryData = RSAProvider.encryptPrivateKey(data, privateKey);
        if (outFile != null){
            boolean result = FileUtils.saveDataToFile(encryData, outFile);
            Log.d("FileEncryptionManager", "save file result "+result);
        }
        return encryData;
    }

    /**
     * using public key decrypt file
     * @param inputFile the file is encrypted
     * @param outFile
     * @return origial data by decrypted
     */
    public byte[] decryptFileByPublicKey(File inputFile, File outFile) throws Exception {
        if (publicKey == null || publicKey.isEmpty()){
            throw new IllegalArgumentException("PublicKey is empty, you should invoke setRSAKey or generateKey");
        }
        byte[] data = FileUtils.getDataFromFile(inputFile);
        byte[] decryData = RSAProvider.decryptPublicKey(data, publicKey);
        if (outFile != null){
            boolean result = FileUtils.saveDataToFile(decryData, outFile);
            Log.d("FileEncryptionManager", "save file result "+result);
        }
        return decryData;
    }
}