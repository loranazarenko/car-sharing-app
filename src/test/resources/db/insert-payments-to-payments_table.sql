INSERT INTO payments (id, status, type, rental_id, session_url, session_id, amount_to_pay)
VALUES
    (1, 'PAID', 'PAYMENT', 1, 'http://payment.url', '1', 10000.00),
    (2, 'PENDING', 'FINE', 2, 'http://mock.url', 'mockSessionId', 5000.00),
    (3, 'PAID', 'PAYMENT', 3, 'http://payment.url', '2', 15000.00);