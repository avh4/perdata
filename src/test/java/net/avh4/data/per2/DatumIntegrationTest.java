package net.avh4.data.per2;

import org.junit.Before;
import org.junit.Test;
import org.picocontainer.DefaultPicoContainer;
import org.picocontainer.MutablePicoContainer;

import static org.fest.assertions.Assertions.assertThat;

public class DatumIntegrationTest {
    private Database db;

    public interface Book {
        public String title();
        public Person author();
        public Chapter[] chapters();

        public interface Person {
            public String name();
        }

        public interface Chapter {
            public String title();
            public String body();
        }
    }

    @Before
    public void setUp() throws Exception {
        MutablePicoContainer pico = new DefaultPicoContainer();
        pico.addComponent(Database.class);
        pico.addComponent(MemoryDatumStore.class);
        db = pico.getComponent(Database.class);
    }

    @Test
    public void test1() throws Exception {
        final String[] book = new String[1];
        final String[] author = new String[1];
        db.transaction(new Runnable() {
            @Override public void run() {

                book[0] = db.create();
                db.set(book[0], "title", "The Big Orange Splot");

                author[0] = db.create();
                db.set(author[0], "name", "Daniel Pinkwater");
                db.set(book[0], "author", author[0]);

                String chapter1 = db.create();
                db.set(chapter1, "title", "Chapter 1");
                db.set(chapter1, "body", "Mr. Plumbean lived on a ...");
                db.add(book[0], "chapters", chapter1);
            }
        });

        db.transaction(new Runnable() {
            @Override public void run() {
                db.set(author[0], "name", "Daniel Manus Pinkwater");

                String chapter2 = db.create();
                db.set(chapter2, "title", "Chapter 2");
                db.set(chapter2, "body", "He liked it that ...");
                db.add(book[0], "chapters", chapter2);
            }
        });

        Book[] books = db.query(Book.class);
        assertThat(books).isNotNull().hasSize(1).hasAllElementsOfType(Book.class);

        assertThat(books[0].title()).isNotNull().isEqualTo("The Big Orange Splot");
        assertThat(books[0].author()).isNotNull();
        assertThat(books[0].author().name()).isEqualTo("Daniel Manus Pinkwater");
        assertThat(books[0].chapters()).isNotNull().hasSize(2).hasAllElementsOfType(Book.Chapter.class);
        assertThat(books[0].chapters()[0].title()).isEqualTo("Chapter 1");
        assertThat(books[0].chapters()[0].body()).startsWith("Mr. Plumbean lived on a");
        assertThat(books[0].chapters()[1].title()).isEqualTo("Chapter 2");
        assertThat(books[0].chapters()[1].body()).startsWith("He liked it that");
    }
}
