CREATE TABLE
    IF NOT EXISTS posts (
        id BIGSERIAL PRIMARY KEY,
        title VARCHAR(500) NOT NULL,
        text TEXT NOT NULL,
        tags VARCHAR(1000),
        likes_count INTEGER DEFAULT 0,
        comments_count INTEGER DEFAULT 0,
        image BYTEA
    );

CREATE TABLE
    IF NOT EXISTS comments (
        id BIGSERIAL PRIMARY KEY,
        text VARCHAR(1000) NOT NULL,
        post_id BIGINT NOT NULL REFERENCES posts (id) ON DELETE CASCADE
    );

INSERT INTO
    posts (title, text, tags, likes_count, comments_count)
VALUES
    (
        'Первый пост',
        'Бывший британский спецназовец Левон Кейд оставил войну в прошлом и теперь работает бригадиром на стройке. Он всегда поможет в трудную минуту и не допустит несправедливости, за что его уважают коллеги. Однажды у близких ему людей бандиты похищают 19-летнюю дочь. Полиция бездействует, и Левон по просьбе родителей решает самостоятельно найти девушку и вернуть её домой.',
        'java,программирование,опыт',
        5,
        2
    ),
    (
        'Второй пост',
        'Сегодня хочу поделиться мыслями о Spring Framework.  А может и не хочу, как пойдет.',
        'spring,framework,java',
        3,
        1
    ),
    (
        'Третий пост',
        'Разбираемся с Hibernate ORM и его преимуществами перед JDBC.',
        'hibernate,orm,database',
        8,
        3
    );

INSERT INTO
    comments (text, post_id)
VALUES
    ('Отличный пост, спс!', 1),
    (
        'Согласен с автором, Java - прекрасный язык, ведь он произошел от JavaScript',
        1
    ),
    ('Spring действительно упрощает разработку', 2),
    ('Hibernate экономит много времени', 3),
    ('Интересная статья про ORM', 3),
    ('Когда будет продолжение?', 3);