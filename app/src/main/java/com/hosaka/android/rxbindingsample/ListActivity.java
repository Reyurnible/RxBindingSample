package com.hosaka.android.rxbindingsample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.jakewharton.rxbinding.widget.AbsListViewScrollEvent;
import com.jakewharton.rxbinding.widget.RxAbsListView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ListActivity extends AppCompatActivity {
    private static final int LIMIT = 30;

    private ArrayAdapter<String> mAdapter;
    private boolean mIsRequest = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ListView listView = (ListView) findViewById(R.id.listview);
        mAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.item_list);
        listView.setAdapter(mAdapter);

        RxAbsListView.scrollEvents(listView)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(Schedulers.io())
                // スクロール位置が下から3つ目までスクロールしたかを見る
                .filter(getEndScrollFilter(3))
                // リクエスト処理を行っているかどうが処理をブロックします
                .filter(new Func1<AbsListViewScrollEvent, Boolean>() {
                    @Override
                    public Boolean call(AbsListViewScrollEvent scrollEvent) {
                        return !mIsRequest;
                    }
                })
                // データ取得のObservableに処理を繋げます
                .flatMap(new Func1<AbsListViewScrollEvent, Observable<List<String>>>() {
                    @Override
                    public Observable<List<String>> call(AbsListViewScrollEvent scrollEvent) {
                        return getDataObservable(scrollEvent.totalItemCount(), LIMIT);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> strings) {
                        mIsRequest = false;
                        mAdapter.addAll(strings);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        mIsRequest = false;
                        Toast.makeText(getApplicationContext(), "失敗しました", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private Func1<AbsListViewScrollEvent, Boolean> getEndScrollFilter(final int space) {
        return new Func1<AbsListViewScrollEvent, Boolean>() {
            @Override
            public Boolean call(AbsListViewScrollEvent scrollEvent) {
                return scrollEvent.firstVisibleItem() + scrollEvent.visibleItemCount() + space >= scrollEvent.totalItemCount();
            }
        };
    }

    private Observable<List<String>> getDataObservable(final int offset, final int limit) {
        mIsRequest = true;
        final List<String> data = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            data.add(String.valueOf(i + offset));
        }
        return Observable.just(data);
    }

}
