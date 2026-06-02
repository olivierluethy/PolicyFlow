-- PolicyFlow initial schema
-- All monetary values are stored as DECIMAL(10,2) in Swiss francs (CHF).

CREATE TABLE customers (
    id            BIGSERIAL PRIMARY KEY,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(255) UNIQUE NOT NULL,
    phone         VARCHAR(50),
    date_of_birth DATE NOT NULL,
    canton        VARCHAR(2) NOT NULL,
    address       VARCHAR(255),
    city          VARCHAR(100),
    zip           VARCHAR(10),
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE insurance_providers (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(20) UNIQUE NOT NULL,
    name         VARCHAR(100) NOT NULL,
    logo_url     VARCHAR(255),
    base_premium DECIMAL(10,2) NOT NULL,
    active       BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE offers (
    id               BIGSERIAL PRIMARY KEY,
    customer_id      BIGINT NOT NULL REFERENCES customers(id),
    provider_id      BIGINT NOT NULL REFERENCES insurance_providers(id),
    canton           VARCHAR(2) NOT NULL,
    age              INT NOT NULL,
    franchise        INT NOT NULL,
    unfalleinschluss BOOLEAN NOT NULL DEFAULT FALSE,
    monthly_premium  DECIMAL(10,2) NOT NULL,
    yearly_premium   DECIMAL(10,2) NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    valid_until      DATE,
    notes            TEXT,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE policies (
    id              BIGSERIAL PRIMARY KEY,
    offer_id        BIGINT UNIQUE NOT NULL REFERENCES offers(id),
    customer_id     BIGINT NOT NULL REFERENCES customers(id),
    provider_id     BIGINT NOT NULL REFERENCES insurance_providers(id),
    policy_number   VARCHAR(50) UNIQUE NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    start_date      DATE NOT NULL,
    end_date        DATE,
    monthly_premium DECIMAL(10,2) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE appointments (
    id               BIGSERIAL PRIMARY KEY,
    customer_id      BIGINT NOT NULL REFERENCES customers(id),
    title            VARCHAR(255) NOT NULL,
    description      TEXT,
    appointment_date TIMESTAMP NOT NULL,
    status           VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    created_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE audit_log (
    id           BIGSERIAL PRIMARY KEY,
    entity_type  VARCHAR(50) NOT NULL,
    entity_id    BIGINT NOT NULL,
    action       VARCHAR(50) NOT NULL,
    description  TEXT NOT NULL,
    performed_by VARCHAR(100),
    performed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    username      VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(20) NOT NULL,
    full_name     VARCHAR(200),
    active        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for the most common access paths.
CREATE INDEX idx_customers_canton       ON customers(canton);
CREATE INDEX idx_offers_customer        ON offers(customer_id);
CREATE INDEX idx_offers_status          ON offers(status);
CREATE INDEX idx_policies_customer      ON policies(customer_id);
CREATE INDEX idx_policies_status        ON policies(status);
CREATE INDEX idx_appointments_customer  ON appointments(customer_id);
CREATE INDEX idx_appointments_date      ON appointments(appointment_date);
CREATE INDEX idx_audit_entity           ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_performed_at     ON audit_log(performed_at);
