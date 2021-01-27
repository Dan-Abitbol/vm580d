package il.co.ytcom.vm580d;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import crypto.Preferences;
import utils.Base64Utils;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class CryptoService extends IntentService {


    public void setPublicKey(PublicKey publicKey, String key) throws Exception {

        byte[] pubKey = publicKey.getEncoded();
        String pubKeyString = Base64Utils.encode(pubKey);
        Preferences.getInstance().setString(key,pubKeyString );
    }

    public PublicKey getPublicKey(String key) {

        PublicKey pKey = null;
        try {

            String pubString = Preferences.getInstance().getString(key,null);

            if(pubString!=null) {
                byte[] binCpk = Base64Utils.decode(pubString);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(binCpk);
                pKey = keyFactory.generatePublic(publicKeySpec);
            }
        }catch(Exception e){
        }
        return pKey;
    }


    public PrivateKey getPrivateKey(String key) throws Exception{

        PrivateKey privateKey = null;

        String privateString = Preferences.getInstance().getString(key,null);
        if(privateString!=null){
            byte[] binCpk = Base64Utils.decode(privateString);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(binCpk);
            privateKey = keyFactory.generatePrivate(privateKeySpec);
        }
        return privateKey;
    }


    private void EncryptFile(SecretKey secretKey, String inputFile, String outputFile) throws NoSuchPaddingException, NoSuchAlgorithmException, IOException, InvalidKeyException {
        FileInputStream inputStream = new FileInputStream(inputFile);
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

        int read;
        byte[] buffer = new byte[4096];

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        //AES/ECB/PKCS5Padding
        CipherInputStream cis = new CipherInputStream(inputStream, cipher);

        while ((read = cis.read(buffer)) != -1) {
            fileOutputStream.write(buffer, 0, read);
        }
        fileOutputStream.close();
        cis.close();
    }

    String ComputeHash(String fileName) throws Exception {
        byte[] buffer= new byte[8192];
        int count;
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName));
        while ((count = bis.read(buffer)) > 0) {
            digest.update(buffer, 0, count);
        }
        bis.close();

        byte[] hash = digest.digest();
        return Base64Utils.encode(hash);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void EncodeAndSign(String plainText) {

        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGen.init(256); // for example
        SecretKey secretKey = keyGen.generateKey();

        String destFile = plainText.substring(plainText.lastIndexOf(File.separator),plainText.lastIndexOf('.'));

        File default_folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        try {
//            EncryptFile(secretKey, plainText, destFile + "_ENC.mp4");

            String h = ComputeHash(plainText);

            // ------------------------

//            final Cipher cipher = Cipher.getInstance("RSA");
//            cipher.init(Cipher.ENCRYPT_MODE, getPublicKey("keyP"));
////            byte[] encryptedData = cipher.doFinal(secretKey.getEncoded());
//            byte[] encryptedData = cipher.doFinal(h.getBytes());

            try {
//                final String encryptedText = Base64Utils.encode(encryptedData);//, "UTF-8");
                final String encryptedText = Base64Utils.encode(h.getBytes());//, "UTF-8");



                Signature privateSignature = Signature.getInstance("SHA256withRSA");

                privateSignature.initSign(getPrivateKey("keyP"));
                privateSignature.update(secretKey.getEncoded());

                FileOutputStream fileOutputStream = new FileOutputStream(default_folder.getCanonicalPath() + File.separator + destFile+"_HASH.sig");
                fileOutputStream.write(encryptedText.toString().getBytes());
//                fileOutputStream.write('\n');
//                fileOutputStream.write('\n');
                //append signature to file
                fileOutputStream.write(Base64Utils.encode(privateSignature.sign()).getBytes());
                fileOutputStream.close();

            }
            catch (final UnsupportedEncodingException e1) {
                e1.printStackTrace();
                return ;
            }

//            return Base64.getEncoder().encodeToString(signature);



        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException  | SignatureException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        byte[] result = mFileEncryptionManager.encryptFileByPublicKey(ogirialFile, saveEncryFile);
    }


    private static final String ACTION_ENCRYPT_FILE = "il.co.ytcom.vm580d.action.ENCRYPT";

    // TODO: Rename parameters
    private static final String EXTRA_FILENAME = "il.co.ytcom.vm580d.extra.FILENAME";

    public CryptoService() {
        super("CryptoService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionEncrypt(Context context, String fileName) {
        Intent intent = new Intent(context, CryptoService.class);
        intent.setAction(ACTION_ENCRYPT_FILE);
        intent.putExtra(EXTRA_FILENAME, fileName);
        context.startService(intent);
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_ENCRYPT_FILE.equals(action)) {
                final String filename = intent.getStringExtra(EXTRA_FILENAME);
                EncodeAndSign(filename);
            } else {
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionFoo(String param1, String param2) {
        // TODO: Handle action Foo
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }
}