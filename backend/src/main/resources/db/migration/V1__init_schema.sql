CREATE TABLE roles (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(160) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role_id BIGINT NOT NULL REFERENCES roles(id),
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE recs (
  id BIGSERIAL PRIMARY KEY,
  rec_code VARCHAR(40) NOT NULL UNIQUE,
  producer_id BIGINT NOT NULL REFERENCES users(id),
  energy_source VARCHAR(40) NOT NULL,
  generation_start_date DATE NOT NULL,
  generation_end_date DATE NOT NULL,
  energy_quantity_mwh NUMERIC(14,3) NOT NULL CHECK (energy_quantity_mwh > 0),
  certificate_quantity INTEGER NOT NULL CHECK (certificate_quantity > 0),
  status VARCHAR(30) NOT NULL,
  created_by BIGINT NOT NULL REFERENCES users(id),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  CHECK (generation_end_date >= generation_start_date)
);

CREATE TABLE rec_status_history (
  id BIGSERIAL PRIMARY KEY,
  rec_id BIGINT NOT NULL REFERENCES recs(id),
  old_status VARCHAR(30),
  new_status VARCHAR(30) NOT NULL,
  changed_by BIGINT NOT NULL REFERENCES users(id),
  comment VARCHAR(500),
  changed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_role_id ON users(role_id);
CREATE INDEX idx_recs_producer_id ON recs(producer_id);
CREATE INDEX idx_recs_status ON recs(status);
CREATE INDEX idx_recs_energy_source ON recs(energy_source);
CREATE INDEX idx_recs_generation_start_date ON recs(generation_start_date);
CREATE INDEX idx_recs_created_by ON recs(created_by);
CREATE INDEX idx_rec_status_history_rec_id ON rec_status_history(rec_id);
