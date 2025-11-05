docker build -t user-service:latest .
docker tag user-service:latest 855607364597.dkr.ecr.ap-northeast-2.amazonaws.com/primes/user-service:latest

aws ecr get-login-password --region ap-northeast-2 \
| docker login --username AWS --password-stdin 855607364597.dkr.ecr.ap-northeast-2.amazonaws.com
docker push 855607364597.dkr.ecr.ap-northeast-2.amazonaws.com/primes/user-service:latest
