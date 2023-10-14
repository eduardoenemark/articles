package br.com.enemark.articles.deepcopy;

import lombok.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static br.com.enemark.articles.deepcopy.Copyer.deepCopy;
import static org.junit.jupiter.api.Assertions.*;

class DeepCopyTest {

    @Test
    @DisplayName("Sample test copy of A")
    void sampleCopyOfA() throws IOException, ClassNotFoundException {
        final int x1 = 10;

        var a = new A(x1);
        var copyOfA = (A) deepCopy(a);

        final int x2 = 20;
        a.setX(x2);

        assertEquals(x2, a.getX());
        assertEquals(x1, copyOfA.getX());
        assertNotEquals(copyOfA.hashCode(), a.hashCode());
        assertNotEquals(copyOfA, a);
    }

    @Test
    @DisplayName("Copy of B")
    void copyOfB() throws IOException, ClassNotFoundException {
        final int x1 = 10;
        final var y1 = "y of B";
        final var z1 = Long.valueOf(19140728);

        var b = new B(x1, y1, z1);
        var copyOfB = (B) deepCopy(b);

        final int x2 = 20;
        final var y2 = "y of B copy";
        final var z2 = Long.valueOf(19181111);

        b.setX(x2);
        b.setY(y2);
        b.setZ(z2);

        assertEquals(x2, b.getX());
        assertEquals(y2, b.getY());
        assertEquals(z2, b.getZ());

        assertEquals(x1, copyOfB.getX());
        assertEquals(y1, copyOfB.getY());
        assertEquals(z1, copyOfB.getZ());

        assertNotEquals(copyOfB.hashCode(), b.hashCode());
        assertNotEquals(copyOfB, b);
    }

    @Test
    @DisplayName("Copy of C")
    void copyOfC() throws IOException, ClassNotFoundException {
        final int x1 = 10;
        final var y1 = "y of C";
        final var z1 = Long.valueOf(19140728);
        final List<A> as1 = Arrays.asList(new A(1), new A(2));
        final E[] es1 = new E[]{E.V1, E.V3};

        var c = new C(x1, y1, z1, as1, es1);
        var copyOfC = (C) deepCopy(c);

        final int x2 = 20;
        final var y2 = "y of C copy";
        final var z2 = Long.valueOf(19181111);
        final List<A> as2 = List.of(new A(3));
        final E[] es2 = new E[]{E.V2};

        c.setX(x2);
        c.setY(y2);
        c.setZ(z2);
        c.setAs(as2);
        c.setEs(es2);

        assertEquals(x2, c.getX());
        assertEquals(y2, c.getY());
        assertEquals(z2, c.getZ());

        assertEquals(as2.size(), c.getAs().size());
        assertTrue(c.getAs().containsAll(as2));

        assertEquals(es2.length, c.getEs().length);
        assertArrayEquals(es2, c.getEs());

        assertEquals(x1, copyOfC.getX());
        assertEquals(y1, copyOfC.getY());
        assertEquals(z1, copyOfC.getZ());

        assertEquals(as1.size(), copyOfC.getAs().size());
        assertTrue(copyOfC.getAs().containsAll(as1));

        assertEquals(es1.length, copyOfC.getEs().length);
        assertArrayEquals(es1, copyOfC.getEs());

        assertNotEquals(copyOfC.hashCode(), c.hashCode());
        assertNotEquals(copyOfC, c);
    }

    @Test
    @DisplayName("Copy of Map")
    void copyOfMap() throws IOException, ClassNotFoundException {
        var map1 = new HashMap<Integer, String>();
        map1.put(1, "string1");
        map1.put(2, "string2");

        Map<Short, String> map2 = (Map<Short, String>) deepCopy(map1);
        map1.clear();

        assertEquals(0, map1.size());
        assertEquals(2, map2.size());

        assertEquals("string1", map2.get(1));
        assertEquals("string2", map2.get(2));
    }

    @Test
    @DisplayName("Copy of D: static attribute")
    void copyOfD() throws IOException, ClassNotFoundException {
        final var ref1 = new AtomicReference<>(10.10D);
        final var ref2 = new AtomicReference<>(6.66D);
        var d1 = D.of();
        d1.reference = ref1;

        var d2 = (D) deepCopy(d1);
        d1.reference = ref2;

        assertNotNull(d2);
        assertNotNull(d2.reference);

        assertEquals(d1.reference, ref2);
        assertEquals(d2.reference, ref2);
        assertEquals(D.reference, ref2);
    }
}

final class Copyer {
    public static Object deepCopy(Object obj) throws IOException, ClassNotFoundException {
        try (var bos = new ByteArrayOutputStream();
             var oos = new ObjectOutputStream(bos)) {
            oos.writeObject(obj);
            oos.flush();
            try (var bais = new ByteArrayInputStream(bos.toByteArray());
                 var ois = new ObjectInputStream(bais)) {
                return ois.readObject();
            }
        }
    }
}

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
class A implements Serializable {
    private int x;
}

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
class B extends A implements Serializable {
    private String y;
    private Long z;

    B(int x, String y, Long z) {
        super(x);
        this.y = y;
        this.z = z;
    }
}

enum E {
    V1, V2, V3
}

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
class C extends B implements Serializable {
    private List<A> as = new LinkedList<>();
    private E[] es = new E[]{E.V1, E.V2};

    C(int x, String y, Long z, List<A> as, E[] es) {
        super(x, y, z);
        this.as = as;
        this.es = es;
    }
}

@Data(staticConstructor = "of")
class D implements Serializable {
    public static AtomicReference<Double> reference;
}
