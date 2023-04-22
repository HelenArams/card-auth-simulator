-- Demo cards. Tokens stand in for PANs; only the last four digits are kept.
-- They map to the well-known scheme test numbers:
--   tok_visa_1111  Visa        4111 1111 1111 1111  active, plenty of balance
--   tok_mc_4444    Mastercard  5555 5555 5555 4444  active, nearly empty
--   tok_visa_5556  Visa        4000 0566 5566 5556  blocked by the issuer
--   tok_visa_4242  Visa        4242 4242 4242 4242  expired in 2021
-- Amounts are in minor units (cents).
INSERT INTO cards (token, last4, expiry, status, balance, held, currency, daily_limit, version) VALUES
  ('tok_visa_1111', '1111', '2029-12', 'ACTIVE',  250000, 0, 'EUR', 100000, 0),
  ('tok_mc_4444',   '4444', '2029-12', 'ACTIVE',    1500, 0, 'EUR', 100000, 0),
  ('tok_visa_5556', '5556', '2029-12', 'BLOCKED',  50000, 0, 'EUR', 100000, 0),
  ('tok_visa_4242', '4242', '2021-06', 'ACTIVE',   50000, 0, 'EUR', 100000, 0);
