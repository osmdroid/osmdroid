package org.osmdroid.views.overlay;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Fabrice Fontaine
 * @since 6.1.0
 * Triggered by https://github.com/osmdroid/osmdroid/issues/1260
 */
public class DefaultOverlayManagerTest {

    private static final int LOOPS = 10000;

    @Test
    public void testOverlaysReversed() {
        final ListTest<Overlay> list = new ListTest<Overlay>() {

            private final DefaultOverlayManager defaultOverlayManager = new DefaultOverlayManager(null);

            @Override
            public void add() {
                defaultOverlayManager.add(new Overlay() {
                });
            }

            @Override
            public void remove() {
                defaultOverlayManager.remove(0);
            }

            @Override
            public Iterable<Overlay> reverseOrder() {
                return defaultOverlayManager.overlaysReversed();
            }

            @Override
            protected ListIterator<Overlay> unprotectedReverseListIterator() {
                throw new IllegalArgumentException(); // not to be used here
            }

            @Override
            public ListIterator<Overlay> reverseIterator() {
                throw new IllegalArgumentException(); // not to be used here
            }
        };

        final ListTester<Overlay> tester = new ListTester<>();
        tester.test(list);
    }

    /**
     * General tests on the correct use of CopyOnWriteArrayList, using Integer's
     */
    @Test
    public void testIntegerReversed() {
        ListTest<Integer> list;
        final ListTester<Integer> tester = new ListTester<>();

        // best version ever
        list = new ListTest<>();
        tester.test(list);

        // synchronized version: slower, and not bullet-proof in the real world
        list = new ListTest<Integer>() {
            @Override
            public void remove() {
                synchronized (mList) {
                    super.remove();
                }
            }

            @Override
            public ListIterator<Integer> reverseIterator() {
                synchronized (mList) {
                    return unprotectedReverseListIterator();
                }
            }
        };
        tester.test(list);

        // half synchronized version: prone to crash because the item removal is not synchronized
        list = new ListTest<Integer>() {
            @Override
            public ListIterator<Integer> reverseIterator() {
                synchronized (mList) {
                    return unprotectedReverseListIterator();
                }
            }
        };
        //tester.test(list); // don't run this, it would crash!
    }

    /**
     * Generic tester, where we add n items, then in 2 Thread's remove them one by one, and loop
     */
    private class ListTester<T> {

        private Exception mException;

        private void test(final ListTest<T> pList) {
            mException = null;
            for (int i = 0; i < LOOPS; i++) {
                pList.add();
            }
            final Thread remove = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < LOOPS; i++) {
                            pList.remove();
                        }
                    } catch (final Exception e) {
                        mException = e;
                    }
                }
            });
            final Thread loop = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        for (int i = 0; i < LOOPS; i++) {
                            for (final T item : pList.reverseOrder()) {
                                //
                            }
                        }
                    } catch (final Exception e) {
                        mException = e;
                    }
                }
            });
            final long begin = System.currentTimeMillis();
            remove.start();
            loop.start();
            try {
                remove.join();
                loop.join();
            } catch (final InterruptedException e) {
                //
            }
            final long end = System.currentTimeMillis();
            System.out.println("duration: " + (end - begin));
            if (mException != null) {
                Assert.fail(mException.getMessage());
            }
        }
    }

    /**
     * Emulating the use of a CopyOnWriteArrayList as in DefaultOverlayManager
     * This class works perfectly
     */
    private class ListTest<T> {
        protected final CopyOnWriteArrayList<T> mList = new CopyOnWriteArrayList<>();

        public void add() {
            mList.add(null);
        }

        public void remove() {
            mList.remove(0);
        }

        protected ListIterator<T> unprotectedReverseListIterator() {
            return mList.listIterator(mList.size());
        }

        public ListIterator<T> reverseIterator() {
            while (true) {
                try {
                    return unprotectedReverseListIterator();
                } catch (final IndexOutOfBoundsException e) {
                    //
                }
            }
        }

        public Iterable<T> reverseOrder() {
            return new Iterable<T>() {
                @Override
                public Iterator<T> iterator() {
                    final ListIterator<T> i = reverseIterator();

                    return new Iterator<T>() {
                        @Override
                        public boolean hasNext() {
                            return i.hasPrevious();
                        }

                        @Override
                        public T next() {
                            return i.previous();
                        }

                        @Override
                        public void remove() {
                            i.remove();
                        }
                    };
                }
            };
        }
    }
}
