# 7일 게시판 배포 가이드

## 📋 시스템 요구사항

### 개발 환경
- **Java**: 21 (OpenJDK 21)
- **Spring Boot**: 3.5.3
- **Database**: H2 (개발용), MySQL 8.0 (프로덕션)
- **Build Tool**: Gradle 8.x

### 프로덕션 환경
- **Java**: 21 (OpenJDK 21)
- **MySQL**: 8.0 이상
- **Docker**: 20.10 이상 (Docker 배포 시)
- **Memory**: 최소 2GB RAM

---

## 🚀 배포 방법

## 방법 1: Docker Compose 배포 (초보자 추천 ⭐)

### 1단계: 서버 준비
```bash
# Ubuntu/Debian 서버에서
sudo apt update && sudo apt upgrade -y
sudo apt install docker.io docker-compose git

# Docker 서비스 시작
sudo systemctl start docker
sudo systemctl enable docker

# 현재 사용자를 docker 그룹에 추가 (재로그인 필요)
sudo usermod -aG docker $USER
```

### 2단계: 프로젝트 배포
```bash
# 프로젝트 클론
git clone <your-repository-url>
cd 7ddev_board

# 환경 변수 설정 (선택사항)
export DB_PASSWORD=your_secure_password
export DB_USERNAME=board_user

# Docker Compose로 전체 스택 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f app
```

### 3단계: 접속 확인
- **게시판**: http://your-server-ip:8080
- **관리자 페이지**: http://your-server-ip:8080/admin
- **기본 관리자 계정**:
  - Username: `admin`
  - Password: `admin123!`

### 4단계: 운영 관리
```bash
# 애플리케이션 재시작
docker-compose restart app

# 전체 중지
docker-compose down

# 데이터 백업
docker-compose exec mysql mysqldump -u board_user -p board_db > backup.sql

# 실시간 로그 확인
docker-compose logs -f app

# 컨테이너 상태 확인
docker-compose ps
```

---

## 방법 2: 수동 설치 (고급 사용자)

### 1단계: Java 21 설치
```bash
# Ubuntu/Debian
sudo apt install openjdk-21-jdk

# CentOS/RHEL
sudo yum install java-21-openjdk-devel

# 설치 확인
java -version
```

### 2단계: MySQL 8.0 설치 및 설정
```bash
# Ubuntu/Debian
sudo apt install mysql-server-8.0

# MySQL 보안 설정
sudo mysql_secure_installation

# MySQL 접속 후 데이터베이스 생성
sudo mysql -u root -p
```

```sql
CREATE DATABASE board_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'board_user'@'%' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON board_db.* TO 'board_user'@'%';
FLUSH PRIVILEGES;
EXIT;
```

### 3단계: 애플리케이션 배포
```bash
# 프로젝트 빌드
./gradlew build -x test

# 환경 변수 설정
export SPRING_PROFILES_ACTIVE=prod
export DB_USERNAME=board_user
export DB_PASSWORD=your_secure_password

# 백그라운드 실행
nohup java -jar build/libs/*.jar > app.log 2>&1 &

# 프로세스 확인
ps aux | grep java
```

---

## 방법 3: 클라우드 플랫폼 배포

### 🔸 AWS EC2 배포 (추천)
1. **EC2 인스턴스 생성**: Ubuntu 22.04, t3.medium 이상
2. **보안 그룹 설정**: 포트 80, 443, 22, 8080 열기
3. **탄력적 IP**: 고정 IP 할당
4. **도메인 연결**: Route 53 또는 외부 DNS

```bash
# EC2 접속 후 Docker 방법 사용
ssh -i your-key.pem ubuntu@your-ec2-ip
# 위의 "방법 1: Docker Compose" 단계 따라하기
```

### 🔸 Google Cloud Run (서버리스)
```bash
# Google Cloud SDK 설치 후
gcloud auth login
gcloud config set project your-project-id

# 컨테이너 이미지 빌드 및 배포
gcloud builds submit --tag gcr.io/your-project-id/board-app
gcloud run deploy board-app --image gcr.io/your-project-id/board-app --platform managed
```

### 🔸 Heroku (가장 간단)
```bash
# Heroku CLI 설치 후
heroku create your-app-name
heroku addons:create cleardb:ignite  # MySQL 애드온

# 환경 변수 설정
heroku config:set SPRING_PROFILES_ACTIVE=prod

# 배포
git push heroku main
```

---

## 🔒 필수 보안 설정

### 1단계: 기본 비밀번호 변경 (반드시!)
```bash
# 애플리케이션 실행 후 http://your-domain:8080/admin 접속
# admin / admin123! 로 로그인 후 비밀번호 즉시 변경
```

### 2단계: 데이터베이스 보안
```bash
# MySQL 설정 파일 편집
sudo nano /etc/mysql/mysql.conf.d/mysqld.cnf

# 외부 접속 제한 추가
bind-address = 127.0.0.1

# MySQL 재시작
sudo systemctl restart mysql
```

### 3단계: 방화벽 설정
```bash
# UFW 방화벽 설정 (Ubuntu)
sudo ufw enable
sudo ufw allow ssh
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8080/tcp
sudo ufw deny 3306/tcp  # MySQL 외부 접속 차단
```

### 4단계: SSL 설정 (프로덕션 필수)
```bash
# Let's Encrypt 인증서 설치
sudo apt install certbot nginx
sudo certbot --nginx -d your-domain.com

# 자동 갱신 설정
sudo crontab -e
# 추가: 0 12 * * * /usr/bin/certbot renew --quiet
```

---

## 📊 모니터링 및 관리

### 애플리케이션 상태 확인
```bash
# 헬스체크
curl http://localhost:8080/actuator/health

# 애플리케이션 정보
curl http://localhost:8080/actuator/info

# 실시간 로그 확인
tail -f logs/board-application.log

# Docker 환경에서
docker-compose logs -f app
```

### 백업 및 복원
```bash
# 데이터베이스 백업
docker-compose exec mysql mysqldump -u board_user -p board_db > backup_$(date +%Y%m%d).sql

# 또는 수동 설치 환경에서
mysqldump -u board_user -p board_db > backup_$(date +%Y%m%d).sql

# 복원
mysql -u board_user -p board_db < backup_20250807.sql
```

### 자동 헬스체크 스크립트
```bash
# health_check.sh 생성
cat > health_check.sh << 'EOF'
#!/bin/bash
if ! curl -f http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "$(date): Application is down! Restarting..."
    docker-compose restart app
    # 또는 수동 설치: systemctl restart board-app
fi
EOF

chmod +x health_check.sh

# 5분마다 자동 체크 (crontab -e에 추가)
*/5 * * * * /path/to/health_check.sh >> /var/log/health_check.log
```

---

## 🚨 트러블슈팅

### 자주 발생하는 문제

#### 1. 애플리케이션이 시작되지 않음
```bash
# 로그 확인
docker-compose logs app
# 또는
tail -f app.log

# 일반적인 원인: 포트 충돌
netstat -tulpn | grep :8080
sudo kill -9 <PID>
```

#### 2. 데이터베이스 연결 실패
```bash
# MySQL 서비스 상태 확인
sudo systemctl status mysql

# MySQL 재시작
sudo systemctl restart mysql

# 연결 테스트
mysql -u board_user -p -h localhost board_db
```

#### 3. 메모리 부족
```bash
# 현재 메모리 사용량 확인
free -h

# JVM 힙 메모리 늘리기
java -Xms1g -Xmx2g -jar build/libs/*.jar
```

#### 4. Docker 관련 문제
```bash
# Docker 서비스 재시작
sudo systemctl restart docker

# 컨테이너 강제 재시작
docker-compose down
docker-compose up -d

# 볼륨 초기화 (주의: 데이터 손실)
docker-compose down -v
docker-compose up -d
```

---

## 🔄 업데이트 방법

### Docker 환경
```bash
# 1. 백업
docker-compose exec mysql mysqldump -u board_user -p board_db > backup_before_update.sql

# 2. 새 이미지 빌드
docker-compose build app

# 3. 무중단 업데이트
docker-compose up -d app

# 4. 확인
docker-compose logs -f app
curl http://localhost:8080/actuator/health
```

### 수동 환경
```bash
# 1. 백업
mysqldump -u board_user -p board_db > backup_before_update.sql

# 2. 애플리케이션 중지
sudo systemctl stop board-app

# 3. 새 버전 빌드
./gradlew build -x test

# 4. 애플리케이션 시작
sudo systemctl start board-app
```

---

## 📞 지원 및 문의

### 문제 발생 시 수집할 정보
1. **에러 로그**: `tail -100 logs/board-application.log`
2. **시스템 정보**: `uname -a`, `java -version`
3. **메모리 사용량**: `free -h`
4. **디스크 사용량**: `df -h`
5. **포트 상태**: `netstat -tulpn | grep 8080`

### 추가 도움말
- GitHub Issues: [프로젝트 Repository]
- 이메일: [support@example.com]

---

## ✅ 배포 체크리스트

### 배포 전
- [ ] Java 21 설치 확인
- [ ] MySQL 8.0 설치 및 설정
- [ ] 방화벽 설정 (포트 80, 443, 8080 열기)
- [ ] 도메인 연결 (선택사항)

### 배포 후
- [ ] 애플리케이션 정상 실행 확인
- [ ] 데이터베이스 연결 확인
- [ ] 기본 기능 동작 테스트
- [ ] 관리자 계정 로그인 및 비밀번호 변경
- [ ] SSL 인증서 설정 (프로덕션)

### 운영 중
- [ ] 정기 백업 설정
- [ ] 로그 모니터링 설정
- [ ] 헬스체크 자동화
- [ ] 보안 업데이트 적용

---

**🎉 축하합니다! 7일 게시판이 성공적으로 배포되었습니다!**
