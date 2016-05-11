package com.hosaka.android.rxbindingsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxTextView;

import java.util.regex.Pattern;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;

public class LoginActivity extends AppCompatActivity {
    // Regex patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9-._/+?]+@[A-Za-z0-9-_]+.[A-Za-z0-9-._]+$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^[a-zA-Z0-9]{8,20}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // FindViews
        AutoCompleteTextView emailEditText = (AutoCompleteTextView) findViewById(R.id.email);
        EditText passwordEditText = (EditText) findViewById(R.id.password);
        Button signinButton = (Button) findViewById(R.id.signin_button);

        Observable<Boolean> emailObservable = observePatternTextChange(emailEditText, EMAIL_PATTERN);
        Observable<Boolean> passwordObservable = observePatternTextChange(passwordEditText, PASSWORD_PATTERN);
        // 上の2つの入力状態の監視結果に変更があった場合に通知が来るようにcombineLatestでまとめて監視を行います
        Observable.combineLatest(emailObservable, passwordObservable,
                new Func2<Boolean, Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean email, Boolean password) {
                        return email && password;
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(RxView.enabled(signinButton));

        signinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(LoginActivity.this, "ログイン処理", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * EditTextなどTextViewの入力の状態を正規表現のPatternと比較して、パターンをみたいしているかBooleanのObservableで結果を返すメソッド
     */
    private Observable<Boolean> observePatternTextChange(TextView textView, final Pattern pattern) {
        return RxTextView.textChanges(textView).map(new Func1<CharSequence, Boolean>() {
            @Override
            public Boolean call(CharSequence charSequence) {
                return pattern.matcher(charSequence).find();
            }
        });
    }

}

