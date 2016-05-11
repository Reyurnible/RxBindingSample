package com.hosaka.android.rxbindingsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;

import com.jakewharton.rxbinding.view.RxView;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {

    Button mLoginButton;
    Button mListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLoginButton = (Button) findViewById(R.id.login_button);
        mListButton = (Button) findViewById(R.id.list_button);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ボタンをクリックした時の処理を指定します. clicksはVoidのObservableで返ってくるためdoOnNextで処理を予め設定しておきます
        Observable<Void> loginClickObservable = RxView.clicks(mLoginButton).doOnNext(getIntentAction(LoginActivity.class));
        Observable<Void> listClickObservable = RxView.clicks(mListButton).doOnNext(getIntentAction(ListActivity.class));
        // 二つの処理の合計実行回数にリミットをかける
        Observable.merge(loginClickObservable, listClickObservable)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .limit(1)
                .subscribe();
    }

    private Action1<Void> getIntentAction(final Class<? extends Activity> clazz) {
        return new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                startActivity(new Intent(getApplicationContext(), clazz));
            }
        };
    }

}
