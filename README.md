# YumYum Docker Setup

`docker-compose.yml` has been added so the project can be started with `frontend`, `backend`, `mysql`, and the `pororo-ocr` service together.

## Quick Start

1. Backend runtime settings are loaded from `BackEnd/src/main/resources/application.properties`.
2. Start containers:

```bash
docker compose up --build
```

## Ports

- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080`
- Pororo OCR: `http://localhost:8001`
- MySQL: `localhost:3307`

## OAuth2 Redirect URIs

Register these callback URLs in each OAuth provider console:

- `http://localhost:8080/login/oauth2/code/google`
- `http://localhost:8080/login/oauth2/code/naver`
- `http://localhost:8080/login/oauth2/code/kakao`

The frontend starts login from `http://localhost:5173` and the backend OAuth2 callback stays on port `8080`, so both ports need to stay open.

## Notes

- On the first MySQL container startup, `docker/mysql/00-init-yumyum.sh` runs `BackEnd/src/main/resources/sql/yumyum.sql` first and then loads `BackEnd/src/main/resources/sql/SSAFY_COACH_Dump.sql` into `food_nutrition`.
- OCR requests now flow through `Pororo OCR -> GPT-5-nano JSON structuring`.
- The first OCR request can take longer because the Pororo model may need to be downloaded inside the `pororo-ocr` container.
- If you need to recreate the database from scratch, run `docker compose down -v` and start again.
