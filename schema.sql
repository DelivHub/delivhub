CREATE TABLE p_category (
                            id           UUID         NOT NULL,
                            name         VARCHAR(100) NOT NULL,
                            is_hidden    BOOLEAN      NOT NULL DEFAULT false,
                            created_at   TIMESTAMP    NOT NULL DEFAULT now(),
                            created_by   VARCHAR(100) NULL,
                            updated_at   TIMESTAMP    NULL,
                            updated_by   VARCHAR(100) NULL,
                            deleted_at   TIMESTAMP    NULL,
                            deleted_by   VARCHAR(100) NULL,

                            CONSTRAINT pk_categories PRIMARY KEY (id),
                            CONSTRAINT uq_categories_name UNIQUE (name)
);

CREATE TABLE p_area (
                        id         UUID         NOT NULL,
                        name       VARCHAR(100) NOT NULL,
                        city       VARCHAR(100) NOT NULL,
                        district   VARCHAR(100) NOT NULL,
                        is_hidden  BOOLEAN    NOT NULL DEFAULT false,
                        created_at TIMESTAMP    NOT NULL DEFAULT now(),
                        created_by VARCHAR(100) NULL,
                        updated_at TIMESTAMP    NULL,
                        updated_by VARCHAR(100) NULL,
                        deleted_at TIMESTAMP    NULL,
                        deleted_by VARCHAR(100) NULL,

                        CONSTRAINT pk_areas PRIMARY KEY (id),
                        CONSTRAINT uq_areas_name UNIQUE (name)
);

CREATE TABLE p_user (
                        username   VARCHAR(10)  NOT NULL,
                        email      VARCHAR(255) NOT NULL,
                        password   VARCHAR(255) NOT NULL,
                        role       VARCHAR(20)  NOT NULL,
                        nickname   VARCHAR(100) NOT NULL,
                        is_public  BOOLEAN      NOT NULL DEFAULT true,
                        created_at TIMESTAMP    NOT NULL DEFAULT now(),
                        created_by VARCHAR(100) NULL,
                        updated_at TIMESTAMP    NULL,
                        updated_by VARCHAR(100) NULL,
                        deleted_at TIMESTAMP    NULL,
                        deleted_by VARCHAR(100) NULL,

                        CONSTRAINT pk_users      PRIMARY KEY (username),
                        CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE p_address (
                           id         UUID         NOT NULL,
                           user_id    VARCHAR(10)  NOT NULL,
                           alias      VARCHAR(50)  NULL,
                           address    VARCHAR(255) NOT NULL,
                           detail     VARCHAR(255) NOT NULL,
                           zip_code   VARCHAR(10)  NOT NULL,
                           is_default BOOLEAN      NOT NULL DEFAULT false,
                           created_at TIMESTAMP    NOT NULL DEFAULT now(),
                           created_by VARCHAR(100) NULL,
                           updated_at TIMESTAMP    NULL,
                           updated_by VARCHAR(100) NULL,
                           deleted_at TIMESTAMP    NULL,
                           deleted_by VARCHAR(100) NULL,

                           CONSTRAINT pk_addresses      PRIMARY KEY (id),
                           CONSTRAINT fk_addresses_user FOREIGN KEY (user_id) REFERENCES p_user (username)
);

CREATE TABLE p_store (
                         id             UUID           NOT NULL,
                         owner_id       VARCHAR(10)    NOT NULL,
                         category_id    UUID           NOT NULL,
                         area_id        UUID           NOT NULL,
                         name           VARCHAR(255)   NOT NULL,
                         address        TEXT           NOT NULL,
                         is_hidden      BOOLEAN        NOT NULL DEFAULT false,
                         average_rating NUMERIC(2, 1)  NOT NULL DEFAULT 0.0,
                         created_at     TIMESTAMP      NOT NULL DEFAULT now(),
                         created_by     VARCHAR(100)   NULL,
                         updated_at     TIMESTAMP      NULL,
                         updated_by     VARCHAR(100)   NULL,
                         deleted_at     TIMESTAMP      NULL,
                         deleted_by     VARCHAR(100)   NULL,

                         CONSTRAINT pk_stores          PRIMARY KEY (id),
                         CONSTRAINT fk_stores_owner    FOREIGN KEY (owner_id)    REFERENCES p_user      (username),
                         CONSTRAINT fk_stores_category FOREIGN KEY (category_id) REFERENCES p_category  (id),
                         CONSTRAINT fk_stores_area     FOREIGN KEY (area_id)     REFERENCES p_area      (id)
);

CREATE TABLE p_menu (
                        id          UUID           NOT NULL,
                        store_id    UUID           NOT NULL,
                        name        VARCHAR(100)   NOT NULL,
                        price       INTEGER        NOT NULL,
                        description TEXT           NULL,
                        is_hidden   BOOLEAN        NOT NULL DEFAULT false,
                        created_at  TIMESTAMP      NOT NULL DEFAULT now(),
                        created_by  VARCHAR(100)   NULL,
                        updated_at  TIMESTAMP      NULL,
                        updated_by  VARCHAR(100)   NULL,
                        deleted_at  TIMESTAMP      NULL,
                        deleted_by  VARCHAR(100)   NULL,

                        CONSTRAINT pk_menus       PRIMARY KEY (id),
                        CONSTRAINT fk_menus_store FOREIGN KEY (store_id) REFERENCES p_store (id)
);

CREATE TABLE p_option (
                          id          UUID           NOT NULL,
                          menu_id     UUID           NOT NULL,
                          name        VARCHAR(100)   NOT NULL,
                          type        VARCHAR(20)    NOT NULL,
                          created_at  TIMESTAMP      NOT NULL DEFAULT now(),
                          created_by  VARCHAR(100)   NULL,
                          updated_at  TIMESTAMP      NULL,
                          updated_by  VARCHAR(100)   NULL,
                          deleted_at  TIMESTAMP      NULL,
                          deleted_by  VARCHAR(100)   NULL,

                          CONSTRAINT pk_options      PRIMARY KEY (id),
                          CONSTRAINT fk_options_menu FOREIGN KEY (menu_id) REFERENCES p_menu (id)
);

CREATE TABLE p_option_item (
                               id          UUID           NOT NULL,
                               option_id   UUID           NOT NULL,
                               name        VARCHAR(100)   NOT NULL,
                               extra_price BIGINT         NOT NULL DEFAULT 0,
                               created_at  TIMESTAMP      NOT NULL DEFAULT now(),
                               created_by  VARCHAR(100)   NULL,
                               updated_at  TIMESTAMP      NULL,
                               updated_by  VARCHAR(100)   NULL,
                               deleted_at  TIMESTAMP      NULL,
                               deleted_by  VARCHAR(100)   NULL,

                               CONSTRAINT pk_option_items        PRIMARY KEY (id),
                               CONSTRAINT fk_option_items_option FOREIGN KEY (option_id) REFERENCES p_option (id)
);

CREATE TABLE p_order (
                         id          UUID           NOT NULL,
                         user_id     VARCHAR(10)    NOT NULL,
                         store_id    UUID           NOT NULL,
                         address_id  UUID           NOT NULL,
                         total_price NUMERIC(10, 2) NOT NULL,
                         request     VARCHAR(255)   NULL,
                         status      VARCHAR(50)    NOT NULL DEFAULT 'PENDING',
                         order_type  VARCHAR(20)    NOT NULL DEFAULT 'ONLINE',
                         created_at  TIMESTAMP      NOT NULL DEFAULT now(),
                         created_by  VARCHAR(100)   NULL,
                         updated_at  TIMESTAMP      NULL,
                         updated_by  VARCHAR(100)   NULL,
                         deleted_at  TIMESTAMP      NULL,
                         deleted_by  VARCHAR(100)   NULL,

                         CONSTRAINT pk_orders         PRIMARY KEY (id),
                         CONSTRAINT fk_orders_user    FOREIGN KEY (user_id)    REFERENCES p_user     (username),
                         CONSTRAINT fk_orders_store   FOREIGN KEY (store_id)   REFERENCES p_store    (id),
                         CONSTRAINT fk_orders_address FOREIGN KEY (address_id) REFERENCES p_address  (id)
);

CREATE TABLE p_order_item (
                              id         UUID           NOT NULL,
                              order_id   UUID           NOT NULL,
                              menu_id    UUID           NOT NULL,
                              quantity   INTEGER        NOT NULL CHECK (quantity > 0),
                              unit_price NUMERIC(10, 2) NOT NULL,
                              created_at TIMESTAMP      NOT NULL DEFAULT now(),
                              created_by VARCHAR(100)   NULL,

                              CONSTRAINT pk_order_items        PRIMARY KEY (id),
                              CONSTRAINT fk_order_items_order  FOREIGN KEY (order_id) REFERENCES p_order (id),
                              CONSTRAINT fk_order_items_menu   FOREIGN KEY (menu_id)  REFERENCES p_menu  (id)
);

CREATE TABLE p_payment (
                           id             UUID           NOT NULL,
                           order_id       UUID           NOT NULL,
                           amount         NUMERIC(10, 2) NOT NULL,
                           payment_method VARCHAR(50)    NOT NULL DEFAULT 'CARD',
                           status         VARCHAR(50)    NOT NULL DEFAULT 'PENDING',
                           created_at     TIMESTAMP      NOT NULL DEFAULT now(),
                           created_by     VARCHAR(100)   NULL,
                           updated_at     TIMESTAMP      NULL,
                           updated_by     VARCHAR(100)   NULL,
                           deleted_at     TIMESTAMP      NULL,
                           deleted_by     VARCHAR(100)   NULL,

                           CONSTRAINT pk_payments        PRIMARY KEY (id),
                           CONSTRAINT uq_payments_order  UNIQUE      (order_id),
                           CONSTRAINT fk_payments_order  FOREIGN KEY (order_id) REFERENCES p_order (id)
);

CREATE TABLE p_review (
                          id             UUID           NOT NULL,
                          order_id       UUID           NOT NULL,
                          store_id       UUID           NOT NULL,
                          user_id        VARCHAR(10)    NOT NULL,
                          rating         NUMERIC(2, 1)  NULL,
                          content        TEXT           NULL,
                          image_url      VARCHAR(500)   NULL,
                          created_at     TIMESTAMP      NOT NULL DEFAULT now(),
                          created_by     VARCHAR(100)   NULL,
                          updated_at     TIMESTAMP      NULL,
                          updated_by     VARCHAR(100)   NULL,
                          deleted_at     TIMESTAMP      NULL,
                          deleted_by     VARCHAR(100)   NULL,

                          CONSTRAINT pk_reviews         PRIMARY KEY (id),
                          CONSTRAINT uq_reviews_order   UNIQUE      (order_id),
                          CONSTRAINT fk_reviews_order   FOREIGN KEY (order_id)  REFERENCES p_order (id),
                          CONSTRAINT fk_reviews_store   FOREIGN KEY (store_id)  REFERENCES p_store (id),
                          CONSTRAINT fk_reviews_user    FOREIGN KEY (user_id)   REFERENCES p_user  (username)
);

CREATE TABLE p_ai_log (
                          id            UUID         NOT NULL,
                          user_id       VARCHAR(10)  NOT NULL,
                          request_text  TEXT         NOT NULL,
                          response_text TEXT         NULL,
                          request_type  VARCHAR(30)  NOT NULL,
                          created_at    TIMESTAMP    NOT NULL DEFAULT now(),
                          created_by    VARCHAR(100) NULL,

                          CONSTRAINT pk_ai_request_logs      PRIMARY KEY (id),
                          CONSTRAINT fk_ai_request_logs_user FOREIGN KEY (user_id) REFERENCES p_user (username)
);
