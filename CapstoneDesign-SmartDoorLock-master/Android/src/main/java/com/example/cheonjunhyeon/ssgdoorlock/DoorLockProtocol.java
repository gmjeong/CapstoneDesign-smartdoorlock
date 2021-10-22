package com.example.cheonjunhyeon.ssgdoorlock;

import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.RSAPrivateKeySpec;
import java.util.Arrays;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DoorLockProtocol {

    private final String TAG = "DoorLockProtocol";
    private SharedPreferences pref;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    public static final int STATE_IDLE            = -1;
    public static final int STATE_GET_AES         = 0;
    public static final int STATE_INIT            = 1;
    public static final int STATE_REQ_OPEN        = 2;
    public static final int STATE_RES_OPEN        = 3;
    public static final int STATE_REQ_RESETPASSWD = 4;
    public static final int STATE_RES_RESETPASSWD = 5;
    public static final int STATE_REQ_GET_CODE    = 6;

    private static final int SIZE_LEN = 1;
    private static final int SIZE_SEQ = 1;
    private static final int SIZE_CMD = 1;
    private static final int SIZE_ACK = 1;
    private static final int SIZE_RSA = 4;
    private static final int SIZE_AES = 16;
    private static final int SIZE_PASSWD = 6;
    private static final int SIZE_RES_PASSSCODE = 3;
    private static final int SIZE_PASSCODE = 32;


    public int state;
    private byte cur_seq ;
    private byte[] tx_buffer;

    DoorLockProtocol(InputStream in, OutputStream out, SharedPreferences pref) {
        this.state = this.STATE_IDLE;
        this.cur_seq = 0;
        this.mmInStream = in;
        this.mmOutStream = out;
        this.pref = pref;
    }

    private void write(byte[] msg) throws IOException {
//        System.out.print("write: ");
//        for(int i = 0; i < msg.length; i++) System.out.print((msg[i] & 0xff)+ " ");
//        System.out.println();

        mmOutStream.write(msg);
        mmOutStream.flush();
    }

    public void run(){
        byte[] ipt = new byte[1];
        int size;
        byte cur_len = 0;
        byte len = 0;

        while (true) {
            try {
                byte[] buffer = new byte[1024];
                size = mmInStream.read(buffer);
                cur_len += size;

//                System.out.println("total: " + cur_len);
//                System.out.println("length: " + size);
//                for(int i = 0; i < size; i++) System.out.print((buffer[i] & 0xff)+ " ");
//                System.out.println();

                if (len == 0) {
                    len = buffer[0];
                    ipt = new byte[len];
                    System.arraycopy(buffer, 0, ipt, cur_len - size, size);

                    continue;
                } else if(len > cur_len){
                    System.arraycopy(buffer, 0, ipt, cur_len - size, size);

                    continue;
                } else if(len == cur_len){
                    System.arraycopy(buffer, 0, ipt, cur_len - size, size);

                    for(int i = 0; i < len; i++) System.out.print((ipt[i] & 0xff)+ " ");
                    System.out.println();

                    len = 0;
                    cur_len = 0;
                } else {
                    len = 0;
                    cur_len = 0;
                    continue;
                }
            } catch (IOException e) {
                len = 0;
                cur_len = 0;
                continue;
            }

            try {
                switch (state)
                {
                    case STATE_RES_OPEN:
                    case STATE_INIT:
                        resAck(ipt);
                        break;
                    case STATE_GET_AES:
                        resGetAES(ipt);
                        break;

                    case STATE_REQ_OPEN:
                        resOpenDoor(ipt);
                        break;
                }
            } catch (Exception e) {}
        }
    }

    // AES: 怨듯넻 _____________________________________________________________ DONE
    private void resAck(byte[] buffer) throws Exception {
        Log.d(TAG, "resACK");
        boolean valid = true;
        int idx = 0;
        byte[] dec = null;
        try {
            dec = decrypt(Arrays.copyOfRange(buffer, 2, buffer.length));
        } catch (Exception e) {
            System.out.println(e.toString());
        }


//        System.out.print("dec: ");
//        for(int i = 0; i < dec.length; i++) System.out.print((dec[i] & 0xff)+ " ");
//        System.out.println();

        // Chk cmd
        int state = buffer[1];
        if(state != this.state)     valid = false;

        // Chk seq
        byte seq = dec[idx++];
        if(seq != this.cur_seq + 1) valid = false;
        else this.cur_seq++;

        // Chk ACK
        int ack = dec[idx++];

        if (valid) {
            switch (state) {
                case STATE_INIT:
                case STATE_RES_OPEN:
                case STATE_RES_RESETPASSWD:
                case STATE_REQ_GET_CODE:
                    resetState();
                    break;
            }
        }

        this.state  = STATE_IDLE;
    }
    private void resetState() {
        this.state = DoorLockProtocol.STATE_IDLE;
    }
    // _______________________________________________________________________

    // RSA: ?듭떊 ?곌껐 ?ㅼ젙(AES ?띾뱷)  ____________________________________________ DONE
    public void reqGetAES() throws Exception {
        if (state != DoorLockProtocol.STATE_IDLE)
            throw new Exception("not STATE_IDLE");

        byte size = SIZE_LEN + SIZE_CMD + SIZE_SEQ + SIZE_RSA;

        tx_buffer = new byte[size];
        int idx = 0;

        // Set Len
        tx_buffer[idx++] = size;

        // Set cmd
        state = DoorLockProtocol.STATE_GET_AES;
        tx_buffer[idx++] = (byte) this.state;

        // Set seq
        Random generator = new Random();
        this.cur_seq = (byte) generator.nextInt(256);
        tx_buffer[idx++] = this.cur_seq;

        // Set pub modulus
        byte[] pubM = ByteBuffer.allocate(4).putInt(pref.getInt("pubM",0)).array();
        tx_buffer[idx++] = pubM[3];
        tx_buffer[idx++] = pubM[2];
        tx_buffer[idx++] = pubM[1];
        tx_buffer[idx++] = pubM[0];

        write(tx_buffer);
    }
    private void resGetAES(byte[] buffer) throws Exception {
        boolean valid = true;
        int size = SIZE_LEN + SIZE_SEQ  + SIZE_CMD + SIZE_AES * 4;
        int idx = 0;
        byte[] aes = new byte[SIZE_AES];

        // Chk len
        int len = buffer[idx++];
        if(len != size)             valid = false;

        // Chk cmd
        int state = buffer[idx++];
        if(state != this.state)     valid = false;

        // Chk seq
        int seq = buffer[idx++];
        if(seq != ++this.cur_seq)   valid = false;

        // CHk AES
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Cipher cipher = Cipher.getInstance("RSA");

            byte[] arrByte = ByteBuffer.allocate(4).putInt(pref.getInt("priM",0)).array();
            BigInteger m = new BigInteger(arrByte);
            arrByte = ByteBuffer.allocate(4).putInt(pref.getInt("priE",0)).array();
            BigInteger e = new BigInteger(arrByte);

            RSAPrivateKeySpec rsaPrivateKeySpec = new RSAPrivateKeySpec(m, e);
            PrivateKey priKey = keyFactory.generatePrivate(rsaPrivateKeySpec);
            cipher.init(Cipher.DECRYPT_MODE, priKey);

            byte[] chunk = new byte[3];
            for(int i = 0; i < aes.length; i++){
                chunk[2] = buffer[idx++];
                chunk[1] = buffer[idx++];
                chunk[0] = buffer[idx++];
                idx++;

                aes[i] = cipher.doFinal(chunk)[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
            valid = false;
        }


        if (valid) {
            System.out.print("length: " + aes.length + " / ");
            for(int i = 0; i < aes.length; i++) System.out.print((aes[i] & 0xff) + " ");
            System.out.println();


            String strAES = new String(aes,  "ISO-8859-1");
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("AES", strAES);
            editor.commit();

            this.state = DoorLockProtocol.STATE_IDLE;
            reqInitDoorLock();
        } else {
            this.state = DoorLockProtocol.STATE_IDLE;
            reqGetAES();
        }
    }
    // _______________________________________________________________________

    // AES: Init Doorlock ____________________________________________________ DONE
    public void reqInitDoorLock() throws Exception {
        if (state != DoorLockProtocol.STATE_IDLE) {
            resetState();
            throw new Exception("not STATE_IDLE");
        }

        state = DoorLockProtocol.STATE_INIT;
        int size = SIZE_SEQ + SIZE_PASSWD + SIZE_PASSCODE;
        size = SIZE_LEN + SIZE_CMD + ((size % SIZE_AES == 0) ? SIZE_AES * (size / SIZE_AES) : size + (SIZE_AES - size % SIZE_AES));

        tx_buffer = new byte[size];
        int idx = 0;

        // Set Len
        tx_buffer[idx++] = (byte) size;

        // Set cmd
        tx_buffer[idx++] = (byte) this.state;

        // Set seq
        Random generator = new Random();
        this.cur_seq = (byte) generator.nextInt(253);
        tx_buffer[idx++] = this.cur_seq;

        // Set Passwd
        String strPasswd = pref.getString("passwd", null);
        for (int i = 0; i < SIZE_PASSWD; i++){
            if (strPasswd == null) tx_buffer[idx++] = 0;
            else tx_buffer[idx++] = (byte) (strPasswd.charAt(i) - '0');
        }

        // Set PASSCODE & Save passcode
        byte[] passcode = new byte[SIZE_PASSCODE];
        for (int i = 0; i < SIZE_PASSCODE; i++) {
            passcode[i] = (byte) (generator.nextInt(254) + 2);
            tx_buffer[idx++] = passcode[i];
        }
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("passcode", new String(passcode, "ISO-8859-1"));
        editor.commit();

        byte[] enc = encrypt(Arrays.copyOfRange(tx_buffer, 2, size));
        System.arraycopy(enc, 0, tx_buffer, 2, enc.length);
        write(tx_buffer);
    }
    // _______________________________________________________________________

    // AES: Open ____________________________________________________________ DONE
    public void reqOpenDoor() throws Exception {
        if (state != DoorLockProtocol.STATE_IDLE){
            this.state = DoorLockProtocol.STATE_IDLE;
            throw new Exception("not STATE_IDLE");
        }


        int size = SIZE_SEQ;
        size = SIZE_LEN + SIZE_CMD + ((size % SIZE_AES == 0) ? SIZE_AES * (size / SIZE_AES) : size + (SIZE_AES - size % SIZE_AES));
        tx_buffer = new byte[size];

        int idx = 0;

        // Set Len
        tx_buffer[idx++] = (byte) size;

        // Set cmd
        state = DoorLockProtocol.STATE_REQ_OPEN;
        tx_buffer[idx++] = (byte) this.state;

        // Set seq
        Random generator = new Random();
        this.cur_seq = (byte) generator.nextInt(253);
        tx_buffer[idx++] = this.cur_seq;

        byte[] enc = encrypt(Arrays.copyOfRange(tx_buffer, 2, size));
        System.arraycopy(enc, 0, tx_buffer, 2, enc.length);
        write(tx_buffer);
    }
    private void resOpenDoor(byte[] buffer) throws Exception {
        boolean valid = true;
        int idx = 0;
        byte[] dec = null;
        try {
            dec = decrypt(Arrays.copyOfRange(buffer, 2, buffer.length));
        } catch (Exception e) {
            System.out.println(e.toString());
        }


        // Chk cmd
        int state = buffer[1];
        if(state != this.state)     valid = false;
        System.out.println(valid);

        // Chk seq
        byte seq = dec[idx++];
        if(seq != this.cur_seq + 1) valid = false;
        else this.cur_seq++;

        // Chk question
        int q1 = dec[idx++];
        int q2 = dec[idx++];
        int q3 = dec[idx++];

        if (valid) {
            // Ok
            int size = SIZE_SEQ + SIZE_RES_PASSSCODE;
            size = SIZE_LEN + SIZE_CMD + ((size % SIZE_AES == 0) ? SIZE_AES * (size / SIZE_AES) : size + (SIZE_AES - size % SIZE_AES));
            tx_buffer = new byte[size];
            idx = 0;

            // Set Len
            tx_buffer[idx++] = (byte) size;

            // Set cmd
            this.state = DoorLockProtocol.STATE_RES_OPEN;
            tx_buffer[idx++] = (byte) this.state;

            // Set Seq
            this.cur_seq = (byte) (seq + 1);
            tx_buffer[idx++] = this.cur_seq;

            // Set Answer
            byte[] passcode = pref.getString("passcode", null).getBytes("ISO-8859-1");
            tx_buffer[idx++] = passcode[q1];
            tx_buffer[idx++] = passcode[q2];
            tx_buffer[idx++] = passcode[q3];

            byte[] enc = encrypt(Arrays.copyOfRange(tx_buffer, 2, size));
            System.arraycopy(enc, 0, tx_buffer, 2, enc.length);
            write(tx_buffer);
        }
    }
    // _______________________________________________________________________

    // AES: get code _________________________________________________________ DONE
    public void reqGetCode() throws Exception {
        if (state != DoorLockProtocol.STATE_IDLE) {
            resetState();
            throw new Exception("not STATE_IDLE");
        }

        state = DoorLockProtocol.STATE_REQ_GET_CODE;
        int size = SIZE_SEQ + SIZE_PASSWD;
        size = SIZE_LEN + SIZE_CMD + ((size % SIZE_AES == 0) ? SIZE_AES * (size / SIZE_AES) : size + (SIZE_AES - size % SIZE_AES));

        tx_buffer = new byte[size];
        int idx = 0;

        // Set Len
        tx_buffer[idx++] = (byte) size;

        // Set cmd
        tx_buffer[idx++] = (byte) this.state;

        // Set seq
        Random generator = new Random();
        this.cur_seq = (byte) generator.nextInt(253);
        tx_buffer[idx++] = this.cur_seq;

        // Set Passwd
        String strPasswd = pref.getString("passwd", null);
        for (int i = 0; i < SIZE_PASSWD; i++){
            if (strPasswd == null) tx_buffer[idx++] = 0;
            else tx_buffer[idx++] = (byte) (strPasswd.charAt(i) - '0');
        }

        byte[] enc = encrypt(Arrays.copyOfRange(tx_buffer, 2, size));
        System.arraycopy(enc, 0, tx_buffer, 2, enc.length);
        write(tx_buffer);
    }
    // _______________________________________________________________________

    // AES: Reset passwd _____________________________________________________ DONE
    public void reqResetPasswd() throws Exception {
        if (state != DoorLockProtocol.STATE_IDLE) {
            resetState();
            throw new Exception("not STATE_IDLE");
        }

        int size = SIZE_SEQ;
        size = SIZE_LEN + SIZE_CMD + ((size % SIZE_AES == 0) ? SIZE_AES * (size / SIZE_AES) : size + (SIZE_AES - size % SIZE_AES));
        tx_buffer = new byte[size];

        int idx = 0;

        // Set Len
        tx_buffer[idx++] = (byte) size;

        // Set cmd
        state = DoorLockProtocol.STATE_REQ_RESETPASSWD;
        tx_buffer[idx++] = (byte) this.state;

        // Set seq
        Random generator = new Random();
        this.cur_seq = (byte) generator.nextInt(253);
        tx_buffer[idx++] = this.cur_seq;


        byte[] enc = encrypt(Arrays.copyOfRange(tx_buffer, 2, size));
        System.arraycopy(enc, 0, tx_buffer, 2, enc.length);
        write(tx_buffer);
    }
    private void resResetPasswd(byte[] buffer) throws Exception {
        boolean valid = true;
        int idx = 0;
        byte[] dec = null;
        try {
            dec = decrypt(Arrays.copyOfRange(buffer, 2, buffer.length));
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        // Chk cmd
        int state = buffer[1];
        if(state != this.state)     valid = false;
        System.out.println(valid);

        // Chk seq
        byte seq = dec[idx++];
        if(seq != this.cur_seq + 1) valid = false;
        else this.cur_seq++;

        // Chk question
        int q1 = dec[idx++];
        int q2 = dec[idx++];
        int q3 = dec[idx++];

        if (valid) {
            // Ok
            int size = SIZE_SEQ + SIZE_RES_PASSSCODE;
            size = SIZE_LEN + SIZE_CMD + ((size % SIZE_AES == 0) ? SIZE_AES * (size / SIZE_AES) : size + (SIZE_AES - size % SIZE_AES));
            tx_buffer = new byte[size];
            idx = 0;

            // Set Len
            tx_buffer[idx++] = (byte) size;

            // Set cmd
            this.state = DoorLockProtocol.STATE_RES_RESETPASSWD;
            tx_buffer[idx++] = (byte) this.state;

            // Set Seq
            this.cur_seq = (byte) (seq + 1);
            tx_buffer[idx++] = this.cur_seq;

            // Set Answer
            byte[] passcode = pref.getString("passcode", null).getBytes("ISO-8859-1");
            tx_buffer[idx++] = passcode[q1];
            tx_buffer[idx++] = passcode[q2];
            tx_buffer[idx++] = passcode[q3];

            byte[] enc = encrypt(Arrays.copyOfRange(tx_buffer, 2, size));
            System.arraycopy(enc, 0, tx_buffer, 2, enc.length);
            write(tx_buffer);
        }
    }
    // _______________________________________________________________________


    // ?뷀샇??蹂듯샇???⑥닔 _________________________________________________________ DONE
    private Key getAESKey() throws Exception {
        byte[] aes = pref.getString("AES", null).getBytes("ISO-8859-1");

        if(aes == null) throw new Exception();

        Key key = new SecretKeySpec(aes, "AES");

        return key;
    }
    private byte[] encrypt(byte[] Data) throws Exception {
        byte[] encVal = new byte[Data.length];
        Key key = getAESKey();
        Cipher chiper = Cipher.getInstance("AES/ECB/NoPadding");
        chiper.init(Cipher.ENCRYPT_MODE, key);

        if(Data.length > 16) {
            int from = 0;
            int to = 0;

            while (to != Data.length) {
                if(to + 16 >= Data.length) to = Data.length;
                else to += 16;

                byte[] ipt = Arrays.copyOfRange(Data, from, to);

                System.arraycopy(chiper.doFinal(ipt), 0, encVal, from, SIZE_AES);

                from += 16;
            }
        } else {
            encVal = chiper.doFinal(Data);
        }

//        System.out.print("pre: ");
//        for(int i = 0; i < tx_buffer.length; i++) System.out.print((tx_buffer[i] & 0xff)+ " ");
//        System.out.println();

        return encVal;
    }
    private byte[] decrypt(byte[] encryptedData) throws Exception {
        byte[] decValue = new byte[encryptedData.length];
        Key key = getAESKey();
        Cipher chiper = Cipher.getInstance("AES/ECB/NoPadding");
        chiper.init(Cipher.DECRYPT_MODE, key);

        if(encryptedData.length > 16) {
            int from = 0;
            int to = 0;

            while (to != encryptedData.length) {
                if(to + 16 >= encryptedData.length) to = encryptedData.length;
                else to += 16;

                byte[] ipt = Arrays.copyOfRange(encryptedData, from, to);
                System.arraycopy(chiper.doFinal(ipt), 0, decValue, from, SIZE_AES);

                from += 16;
            }
        } else {
            decValue = chiper.doFinal(encryptedData);
        }

        return decValue;
    }
    // _______________________________________________________________________
}
