CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE rooms (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    number      VARCHAR(50) NOT NULL,
    capacity    INTEGER NOT NULL CHECK (capacity > 0),

    CONSTRAINT uq_rooms_number UNIQUE (number)
);

CREATE TABLE seats (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    number      VARCHAR(50) NOT NULL,
    room_id     UUID NOT NULL,

    CONSTRAINT fk_seats_room FOREIGN KEY (room_id)
        REFERENCES rooms (id) ON DELETE CASCADE,
    CONSTRAINT uq_seats_room_number UNIQUE (room_id, number)
);

CREATE INDEX idx_seats_room_id ON seats (room_id);

CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name  VARCHAR(100) NOT NULL,
    last_name   VARCHAR(100) NOT NULL,
    birthdate   DATE NOT NULL,
    email       VARCHAR(255) NOT NULL,
    password    VARCHAR(255) NOT NULL,
    phone       VARCHAR(20),
    role        VARCHAR(20) NOT NULL,

    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT chk_users_role CHECK (role IN ('CLIENT', 'EMPLOYEE', 'MANAGER'))
);

CREATE TABLE movies (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title       VARCHAR(255) NOT NULL,
    description TEXT,
    duration    BIGINT NOT NULL
);

CREATE TABLE movie_genres (
    movie_id    UUID NOT NULL,
    genre       VARCHAR(20) NOT NULL,

    CONSTRAINT fk_movie_genres_movie FOREIGN KEY (movie_id)
        REFERENCES movies (id) ON DELETE CASCADE,
    CONSTRAINT pk_movie_genres PRIMARY KEY (movie_id, genre),
    CONSTRAINT chk_movie_genres_genre CHECK (
        genre IN ('THRILLER', 'ROMANCE', 'COMEDY', 'DRAMA', 'ACTION', 'SCI_FI', 'FANTASY', 'ANIMATION')
    )
);

CREATE TABLE projections (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    datetime    TIMESTAMPTZ NOT NULL,
    seat_price  NUMERIC(10, 2) NOT NULL CHECK (seat_price >= 0),
    room_id     UUID NOT NULL,
    movie_id    UUID NOT NULL,

    CONSTRAINT fk_projections_room FOREIGN KEY (room_id)
        REFERENCES rooms (id) ON DELETE CASCADE,
    CONSTRAINT fk_projections_movie FOREIGN KEY (movie_id)
        REFERENCES movies (id) ON DELETE CASCADE
);

CREATE INDEX idx_projections_room_id ON projections (room_id);
CREATE INDEX idx_projections_movie_id ON projections (movie_id);
CREATE INDEX idx_projections_datetime ON projections (datetime);

CREATE TABLE reservations (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    status          VARCHAR(20) NOT NULL,
    projection_id   UUID NOT NULL,
    user_id         UUID NOT NULL,

    CONSTRAINT fk_reservations_projection FOREIGN KEY (projection_id)
        REFERENCES projections (id) ON DELETE CASCADE,
    CONSTRAINT fk_reservations_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT chk_reservations_status CHECK (status IN ('PENDING', 'SUCCESS', 'CANCELED'))
);

CREATE INDEX idx_reservations_projection_id ON reservations (projection_id);
CREATE INDEX idx_reservations_user_id ON reservations (user_id);
CREATE INDEX idx_reservations_status ON reservations (status);

CREATE TABLE reservation_seats (
    reservation_id  UUID NOT NULL,
    seat_id         UUID NOT NULL,

    CONSTRAINT pk_reservation_seats PRIMARY KEY (reservation_id, seat_id),
    CONSTRAINT fk_reservation_seats_reservation FOREIGN KEY (reservation_id)
        REFERENCES reservations (id) ON DELETE CASCADE,
    CONSTRAINT fk_reservation_seats_seat FOREIGN KEY (seat_id)
        REFERENCES seats (id) ON DELETE CASCADE
);

CREATE INDEX idx_reservation_seats_seat_id ON reservation_seats (seat_id);
