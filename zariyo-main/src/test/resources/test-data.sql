-- 카테고리 데이터
INSERT INTO categories (category_name) VALUES
('콘서트'),
('뮤지컬'),
('연극');

-- 공연장 데이터
INSERT INTO concert_halls (hall_name, address) VALUES
('올림픽공원 KSPO DOME', '서울특별시 송파구 올림픽로 424'),
('블루스퀘어 마스터카드홀', '서울특별시 용산구 이태원로 294'),
('코엑스 오디토리움', '서울특별시 강남구 영동대로 513');

-- 홀 좌석 데이터 (올림픽공원 KSPO DOME)
INSERT INTO hall_seats (hall_id, seat_grade, block, seat_row, seat_number) VALUES
-- VIP석
(1, 'VIP', 'A블록', '1열', '1번'),
(1, 'VIP', 'A블록', '1열', '2번'),
(1, 'VIP', 'A블록', '1열', '3번'),
(1, 'VIP', 'A블록', '2열', '1번'),
(1, 'VIP', 'A블록', '2열', '2번'),
-- R석
(1, 'R석', 'B블록', '1열', '1번'),
(1, 'R석', 'B블록', '1열', '2번'),
(1, 'R석', 'B블록', '2열', '1번'),
(1, 'R석', 'B블록', '2열', '2번'),
-- S석
(1, 'S석', 'C블록', '1열', '1번'),
(1, 'S석', 'C블록', '1열', '2번'),
(1, 'S석', 'C블록', '2열', '1번');

-- 블루스퀘어 마스터카드홀 좌석 데이터
INSERT INTO hall_seats (hall_id, seat_grade, block, seat_row, seat_number) VALUES
(2, 'VIP', 'A블록', '1열', '1번'),
(2, 'VIP', 'A블록', '1열', '2번'),
(2, 'R석', 'B블록', '1열', '1번'),
(2, 'R석', 'B블록', '1열', '2번'),
(2, 'S석', 'C블록', '1열', '1번'),
(2, 'S석', 'C블록', '1열', '2번');

-- 공연 데이터
INSERT INTO concerts (title, category_id, hall_id, age_limit, description, running_time, reservation_count, start_date, end_date, status, created_at) VALUES
('아이유 콘서트 [The Golden Hour]', 1, 1, '전체관람가', '아이유의 따뜻한 목소리로 전하는 골든 아워 콘서트. 새로운 앨범의 감동을 라이브로 만나보세요', 150, 8500, DATE_ADD(CURDATE(), INTERVAL 10 DAY), DATE_ADD(CURDATE(), INTERVAL 40 DAY), 'ACTIVE', STR_TO_DATE('2025-04-16 10:30:00', '%Y-%m-%d %H:%i:%s')),
('BTS 월드투어 [Yet To Come]', 1, 2, '전체관람가', '방탄소년단의 글로벌 월드투어 서울 공연. 아미들과 함께하는 특별한 시간', 180, 12000, DATE_ADD(CURDATE(), INTERVAL 5 DAY), DATE_ADD(CURDATE(), INTERVAL 35 DAY), 'ACTIVE', STR_TO_DATE('2025-04-14 10:30:00', '%Y-%m-%d %H:%i:%s')),
('BLACKPINK 월드투어 [Born Pink]', 1, 3, '전체관람가', '블랙핑크의 강렬한 퍼포먼스와 히트곡들을 한자리에서 만나는 월드투어 서울 공연', 160, 15000, DATE_ADD(CURDATE(), INTERVAL 15 DAY), DATE_ADD(CURDATE(), INTERVAL 50 DAY), 'ACTIVE', STR_TO_DATE('2025-04-12 10:30:00', '%Y-%m-%d %H:%i:%s')),
('종료된 공연', 1, 1, '전체관람가', '이미 종료된 공연입니다', 120, 500, DATE_SUB(CURDATE(), INTERVAL 30 DAY), DATE_SUB(CURDATE(), INTERVAL 1 DAY), 'ENDED', NOW());

-- 공연 파일 데이터
INSERT INTO concert_files (concert_id, original_file_name, changed_file_name, file_url, file_type) VALUES
(1, 'iu_golden_hour_poster.jpg', 'uuid_iu_golden_hour_poster.jpg', 'https://example.com/files/iu_golden_hour_poster.jpg', 'POSTER'),
(1, 'iu_golden_hour_description.jpg', 'uuid_iu_golden_hour_description.jpg', 'https://example.com/files/iu_golden_hour_description.jpg', 'DESCRIPTION'),
(2, 'bts_yet_to_come_poster.jpg', 'uuid_bts_yet_to_come_poster.jpg', 'https://example.com/files/bts_yet_to_come_poster.jpg', 'POSTER'),
(3, 'blackpink_born_pink_poster.jpg', 'uuid_blackpink_born_pink_poster.jpg', 'https://example.com/files/blackpink_born_pink_poster.jpg', 'POSTER');

-- 좌석 가격 데이터
INSERT INTO seat_prices (concert_id, seat_grade, price) VALUES
-- 아이유 콘서트
(1, 'VIP', 220000.00),
(1, 'R석', 170000.00),
(1, 'S석', 130000.00),
-- BTS 콘서트
(2, 'VIP', 330000.00),
(2, 'R석', 250000.00),
(2, 'S석', 180000.00),
-- BLACKPINK 콘서트
(3, 'VIP', 290000.00),
(3, 'R석', 220000.00),
(3, 'S석', 160000.00);

-- 스케줄 데이터
INSERT INTO schedules (concert_id, schedule_datetime) VALUES
-- 아이유 콘서트 스케줄 (미래 날짜)
(1, DATE_ADD(NOW(), INTERVAL 15 DAY) + INTERVAL 19 HOUR + INTERVAL 30 MINUTE),
(1, DATE_ADD(NOW(), INTERVAL 16 DAY) + INTERVAL 15 HOUR),
(1, DATE_ADD(NOW(), INTERVAL 18 DAY) + INTERVAL 19 HOUR + INTERVAL 30 MINUTE),
-- BTS 콘서트 스케줄
(2, DATE_ADD(NOW(), INTERVAL 10 DAY) + INTERVAL 20 HOUR),
(2, DATE_ADD(NOW(), INTERVAL 12 DAY) + INTERVAL 16 HOUR),
-- BLACKPINK 콘서트 스케줄
(3, DATE_ADD(NOW(), INTERVAL 20 DAY) + INTERVAL 19 HOUR),
(3, DATE_ADD(NOW(), INTERVAL 22 DAY) + INTERVAL 15 HOUR + INTERVAL 30 MINUTE);

-- 스케줄 좌석 데이터 (아이유 콘서트 첫 번째 스케줄만)
INSERT INTO schedule_seats (schedule_id, hall_seat_id, seat_grade, price, status) VALUES
-- VIP석
(1, 1, 'VIP', 220000.00, 'AVAILABLE'),
(1, 2, 'VIP', 220000.00, 'PENDING'),
(1, 3, 'VIP', 220000.00, 'RESERVED'),
(1, 4, 'VIP', 220000.00, 'AVAILABLE'),
(1, 5, 'VIP', 220000.00, 'AVAILABLE'),
-- R석
(1, 6, 'R석', 170000.00, 'AVAILABLE'),
(1, 7, 'R석', 170000.00, 'RESERVED'),
(1, 8, 'R석', 170000.00, 'AVAILABLE'),
(1, 9, 'R석', 170000.00, 'PENDING'),
-- S석
(1, 10, 'S석', 130000.00, 'AVAILABLE'),
(1, 11, 'S석', 130000.00, 'AVAILABLE'),
(1, 12, 'S석', 130000.00, 'RESERVED'); 