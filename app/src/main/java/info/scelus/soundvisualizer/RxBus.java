package info.scelus.soundvisualizer;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;

/**
 * Created by scelus on 02.05.17
 */
public class RxBus {

    private static RxBus instance;

    private final Subject<Object> bus = PublishSubject.create();

    public static RxBus getInstance() {
        if (instance == null) {
            instance = new RxBus();
        }
        return instance;
    }

    private RxBus() {

    }

    public void send(Object o) {
        bus.onNext(o);
    }

    public Observable<Object> getBus() {
        return bus;
    }
}

