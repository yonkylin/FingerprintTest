package yonky.fingerprinttest;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import javax.crypto.Cipher;

/**
 * Created by Administrator on 2018/8/30.
 */

public class FingerprintDialogFragment extends DialogFragment {
    private FingerprintManager fingerprintManager;
    private CancellationSignal mCancellationsSignal;
    private Cipher mCipher;
    private LoginActivity mActivity;
    private TextView errorMsg;

    private boolean isSelfCancelled;

    public void setCipher(Cipher cipher){
        mCipher=cipher;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity =(LoginActivity)getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fingerprintManager=getContext().getSystemService(FingerprintManager.class);
        setStyle(DialogFragment.STYLE_NORMAL,android.R.style.Theme_Material_Light_Dialog);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v=inflater.inflate(R.layout.fingerprint_dialog,container,false);
        errorMsg=v.findViewById(R.id.error_msg);
        TextView cancel=v.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                stopListening();
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        startListening(mCipher);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopListening();
    }

    private void startListening(Cipher cipher){
        isSelfCancelled= false;
        mCancellationsSignal= new CancellationSignal();
        fingerprintManager.authenticate(new FingerprintManager.CryptoObject(cipher), mCancellationsSignal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                if(!isSelfCancelled){
                    errorMsg.setText(errString);
                    if(errorCode==FingerprintManager.FINGERPRINT_ERROR_LOCKOUT){
                        Toast.makeText(mActivity,errString,Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                }
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                errorMsg.setText(helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                Toast.makeText(mActivity,"指纹认证成功",Toast.LENGTH_SHORT).show();
                mActivity.onAuthenticated();
            }

            @Override
            public void onAuthenticationFailed() {
                errorMsg.setText("指纹认证失败，请再试一次");
            }
        },null);

    }

    private void stopListening(){
        if(mCancellationsSignal!=null){
            mCancellationsSignal.cancel();
            mCancellationsSignal=null;
            isSelfCancelled=true;
        }
    }
}
