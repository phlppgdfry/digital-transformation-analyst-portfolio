# Enterprise Integration Lab

Standalone Java/Maven demonstration of partner-message intake, validation, persistence and error handling. It belongs to the portfolio as a technical companion to the ERP integration and purchase-process cases.

## Scenario

A fictional partner sends an order message. The service validates the payload and partner mapping, stores accepted data and returns a clear outcome. Sample files include a valid order, invalid input and unknown partner. All records are synthetic.

## Contents

- `src/` Java REST service and message handling
- `db/` SQL schema and reporting query
- `samples/` example payloads
- `compose.yaml` local database/application setup
- `scripts/` setup, SQL and smoke-test helpers

## Run

Requirements: Docker with Compose and Java/Maven for local development. See the scripts and `compose.yaml` for the runnable setup. Never add real credentials or partner data; use `.env.example` as a template.

## Analyst angle

Use this lab to discuss partner contracts, validation rules, idempotent processing, error categories, data mapping, support ownership and reconciliation. It is a learning prototype and is not a production integration service.

