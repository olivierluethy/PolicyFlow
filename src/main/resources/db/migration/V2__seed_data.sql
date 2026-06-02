-- PolicyFlow seed data
-- Realistic Swiss providers, two application users and ten customers, plus a handful
-- of offers, policies and appointments so the dashboard is populated out of the box.

-- Swiss health-insurance providers with indicative 2024 base monthly premiums.
INSERT INTO insurance_providers (code, name, base_premium, active) VALUES
    ('CSS',       'CSS Versicherung', 387.50, TRUE),
    ('AXA',       'AXA Schweiz',      412.00, TRUE),
    ('HELSANA',   'Helsana',          398.00, TRUE),
    ('SWICA',     'SWICA',            375.00, TRUE),
    ('SANITAS',   'Sanitas',          365.00, TRUE),
    ('CONCORDIA', 'Concordia',        355.00, TRUE);

-- Application users. Passwords (BCrypt, cost 12): admin -> admin123, advisor -> advisor123.
INSERT INTO users (username, password_hash, role, full_name) VALUES
    ('admin',   '$2a$12$r.Ku4d7S3BOIxMXd.1SvEe1PZLiZGVQAAOetr7AtA/NUOo2jQ4fYW', 'ADMIN',   'System Administrator'),
    ('advisor', '$2a$12$4u8/k99H5ULj9rYAblhTiOQLTDmnJQaiX.pq1HExWiRVJBXb.EkHO', 'ADVISOR', 'Hans Muster');

-- Ten realistic Swiss customers.
INSERT INTO customers (first_name, last_name, email, phone, date_of_birth, canton, address, city, zip) VALUES
    ('Anna',    'Müller',     'anna.mueller@email.ch',   '+41 79 123 45 67', '1985-03-15', 'LU', 'Bahnhofstrasse 12',   'Luzern',     '6003'),
    ('Thomas',  'Keller',     'thomas.keller@email.ch',  '+41 78 234 56 78', '1990-07-22', 'ZH', 'Hauptstrasse 45',     'Zürich',     '8001'),
    ('Maria',   'Schmid',     'maria.schmid@email.ch',   '+41 76 345 67 89', '1978-11-08', 'BE', 'Marktgasse 7',        'Bern',       '3011'),
    ('Peter',   'Weber',      'peter.weber@email.ch',    '+41 79 456 78 90', '1995-02-14', 'BS', 'Rheinstrasse 33',     'Basel',      '4001'),
    ('Sandra',  'Fischer',    'sandra.fischer@email.ch', '+41 77 567 89 01', '1982-09-30', 'GE', 'Rue du Rhône 88',     'Genève',     '1204'),
    ('Michael', 'Brunner',    'michael.brunner@email.ch','+41 79 678 90 12', '1970-06-18', 'AG', 'Dorfstrasse 5',       'Aarau',      '5000'),
    ('Laura',   'Zimmermann', 'laura.zimm@email.ch',     '+41 76 789 01 23', '2000-12-01', 'SG', 'Gallusstrasse 14',    'St. Gallen', '9000'),
    ('Daniel',  'Huber',      'daniel.huber@email.ch',   '+41 78 890 12 34', '1965-04-25', 'TI', 'Via Nassa 22',        'Lugano',     '6900'),
    ('Sophie',  'Meier',      'sophie.meier@email.ch',   '+41 79 901 23 45', '1998-08-11', 'VD', 'Avenue de la Gare 3', 'Lausanne',   '1003'),
    ('Jonas',   'Wolf',       'jonas.wolf@email.ch',     '+41 77 012 34 56', '1988-01-27', 'LU', 'Obergrundstrasse 67', 'Luzern',     '6003');

-- Sample offers (premiums are indicative and consistent with monthly * 12 = yearly).
INSERT INTO offers (customer_id, provider_id, canton, age, franchise, unfalleinschluss, monthly_premium, yearly_premium, status, valid_until, notes, created_at) VALUES
    (1, 1, 'LU', 41, 1000, FALSE, 339.81, 4077.72, 'ACCEPTED', CURRENT_DATE + 30, 'Standard model, accident excluded.', NOW() - INTERVAL '20 days'),
    (2, 3, 'ZH', 36, 2500, TRUE,  366.93, 4403.16, 'ACCEPTED', CURRENT_DATE + 30, 'High deductible, accident included.', NOW() - INTERVAL '15 days'),
    (3, 4, 'BE', 47, 500,  FALSE, 373.59, 4483.08, 'PENDING',  CURRENT_DATE + 25, 'Awaiting customer decision.', NOW() - INTERVAL '5 days'),
    (4, 2, 'BS', 31, 300,  TRUE,  543.16, 6517.92, 'PENDING',  CURRENT_DATE + 28, NULL, NOW() - INTERVAL '3 days'),
    (5, 6, 'GE', 43, 1500, FALSE, 350.46, 4205.52, 'PENDING',  CURRENT_DATE + 29, 'Price-sensitive customer.', NOW() - INTERVAL '2 days'),
    (6, 5, 'AG', 55, 2000, FALSE, 282.49, 3389.88, 'REJECTED', CURRENT_DATE + 20, 'Customer chose a competitor.', NOW() - INTERVAL '10 days'),
    (7, 4, 'SG', 25, 2500, TRUE,  234.36, 2812.32, 'PENDING',  CURRENT_DATE + 30, 'Young adult tariff.', NOW() - INTERVAL '1 days');

-- Policies issued from the two accepted offers.
INSERT INTO policies (offer_id, customer_id, provider_id, policy_number, status, start_date, monthly_premium, created_at) VALUES
    (1, 1, 1, 'HR-2026-100417', 'ACTIVE', DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month', 339.81, NOW() - INTERVAL '19 days'),
    (2, 2, 3, 'HR-2026-100892', 'ACTIVE', DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month', 366.93, NOW() - INTERVAL '14 days');

-- Upcoming appointments (relative to the current date so the dashboard always shows them).
INSERT INTO appointments (customer_id, title, description, appointment_date, status) VALUES
    (3, 'Beratungsgespräch Krankenversicherung', 'Review pending offer and answer questions.', NOW() + INTERVAL '2 days' + INTERVAL '9 hours', 'SCHEDULED'),
    (4, 'Vertragsabschluss',                      'Sign the accident-inclusive policy.',       NOW() + INTERVAL '3 days' + INTERVAL '14 hours', 'SCHEDULED'),
    (5, 'Telefonberatung',                        'Discuss deductible options.',               NOW() + INTERVAL '5 days' + INTERVAL '11 hours', 'SCHEDULED'),
    (1, 'Jahresreview Police',                     'Annual policy review.',                     NOW() + INTERVAL '6 days' + INTERVAL '10 hours', 'SCHEDULED'),
    (7, 'Erstberatung',                            'Introductory meeting for young adult.',     NOW() - INTERVAL '4 days' + INTERVAL '15 hours', 'COMPLETED');

-- Seed audit trail so the dashboard's recent activity feed is non-empty.
INSERT INTO audit_log (entity_type, entity_id, action, description, performed_by, performed_at) VALUES
    ('Customer', 1, 'CREATE', 'Created customer Anna Müller',        'admin',   NOW() - INTERVAL '21 days'),
    ('Offer',    1, 'CREATE', 'Created offer for Anna Müller (CSS)', 'advisor', NOW() - INTERVAL '20 days'),
    ('Offer',    1, 'ACCEPT', 'Offer accepted; issued policy HR-2026-100417', 'advisor', NOW() - INTERVAL '19 days'),
    ('Offer',    2, 'ACCEPT', 'Offer accepted; issued policy HR-2026-100892', 'advisor', NOW() - INTERVAL '14 days'),
    ('Offer',    6, 'REJECT', 'Offer rejected',                      'advisor', NOW() - INTERVAL '9 days');

-- Keep the BIGSERIAL sequences ahead of the manually-inserted ids.
SELECT setval('customers_id_seq',           (SELECT MAX(id) FROM customers));
SELECT setval('insurance_providers_id_seq', (SELECT MAX(id) FROM insurance_providers));
SELECT setval('offers_id_seq',              (SELECT MAX(id) FROM offers));
SELECT setval('policies_id_seq',            (SELECT MAX(id) FROM policies));
SELECT setval('appointments_id_seq',        (SELECT MAX(id) FROM appointments));
SELECT setval('audit_log_id_seq',           (SELECT MAX(id) FROM audit_log));
SELECT setval('users_id_seq',               (SELECT MAX(id) FROM users));
