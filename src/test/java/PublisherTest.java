import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by jinyoung.park on 2017. 1. 16..
 */
@Slf4j
public class PublisherTest {

    @Test
    public void makePubTest() {
        log.info("");

        Publisher<Integer> pub = iterPub(Stream.iterate(1, a -> a +1).limit(10).collect(Collectors.toList()));
        Publisher<Integer> pub2 = mapPub(pub, s -> s * 10);
        pub2.subscribe(logSub());
    }

    private Publisher<Integer> mapPub(Publisher<Integer> pub, Function<Integer, Integer> function) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                pub.subscribe(new DelegateSub(sub) {
                    @Override
                    public void onNext(Integer o) {
                        sub.onNext(function.apply(o));
                    }
                });
            }
        };
    }


    private Publisher<Integer> iterPub(List<Integer> list) {
        return new Publisher<Integer>() {
            @Override
            public void subscribe(Subscriber<? super Integer> sub) {
                sub.onSubscribe(new Subscription() {
                    @Override
                    public void request(long l) {
                        log.info("request: {}", l);
                        list.forEach(sub::onNext);
                        sub.onComplete();
                    }

                    @Override
                    public void cancel() {
                        log.info("cancel");
                    }
                });
            }
        };
    }

    private Subscriber<? super Integer> logSub() {
        return new Subscriber<Integer>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                log.info("onSubscribe: {}");
                subscription.request(Long.MAX_VALUE);
            }

            @Override
            public void onNext(Integer i) {
                log.info("onNext: {}", i);
            }

            @Override
            public void onError(Throwable throwable) {
                log.info("onError: {}", throwable);
            }

            @Override
            public void onComplete() {
                log.info("onComplete: {}");
            }
        };
    }
}

class DelegateSub implements Subscriber<Integer> {

    Subscriber sub;

    public DelegateSub(Subscriber sub) {
        this.sub = sub;
    }

    @Override
    public void onSubscribe(Subscription subscription) {
        sub.onSubscribe(subscription);
    }

    @Override
    public void onNext(Integer o) {
        sub.onNext(o);
    }

    @Override
    public void onError(Throwable throwable) {
        sub.onError(throwable);
    }

    @Override
    public void onComplete() {
        sub.onComplete();
    }
}