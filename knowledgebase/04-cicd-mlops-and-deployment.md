# Group 4: CI/CD, MLOps & Deployment Harness

---

## AI Test Harness

**Basic:** An AI test harness is a robot tester that looks at your app's architecture, generates test cases automatically, runs them across the UI, API, and database, and reports what broke.

**Intermediate:** An AI Test Harness is a platform that combines large language model reasoning with deterministic execution pipelines to deliver autonomous, end-to-end test orchestration. Unlike traditional test automation that requires manually written test scripts, an AI Test Harness ingests system architecture — API specifications, UI component trees, data schemas, user journey maps, and release risk signals — and autonomously generates prioritized test plans. These plans are then executed across multiple specialized execution agents: UI agents (browser automation), API agents (contract validation), data agents (data quality checks), and policy agents (security and compliance rules). The platform continuously self-heals by detecting when application interfaces change and automatically updating the corresponding tests. The output is not just pass/fail results but actionable remediation guidance — identifying the root cause of failures and suggesting fixes. This represents a fundamental shift from test automation (scripts written by humans) to test autonomy (tests generated and maintained by AI).

**Synonyms:** Autonomous test platform, AI-driven QA system, self-healing test harness, intelligent test orchestration.

**Example:** The Harness AI Test Automation platform reads a microservice's OpenAPI spec, generates 200+ API test cases covering all endpoints and error paths, runs them against the staging environment, and automatically updates the tests when a response field name changes in a new deployment.

---

## Feature Store

**Basic:** A feature store is a shared library of ready-to-use data ingredients that any AI model in the company can grab and use for training or making predictions — no need to cook the data from scratch every time.

**Intermediate:** A feature store is a centralized data management system designed for machine learning that bridges the gap between data engineering, data science, and ML engineering. It consists of three core components: an offline store containing historical features for model training and batch inference (stored in warehouses like BigQuery, Snowflake, or S3), an online store providing low-latency access to the latest feature values for real-time inference (backed by Redis, DynamoDB, or similar), and a registry that acts as a centralized catalog of all feature definitions, metadata, and lineage. The feature store solves a critical organizational problem: before feature stores, data scientists duplicated feature engineering work across projects, created inconsistent feature definitions, and struggled to get features from training to production without bugs. By providing a single source of truth with consistent computation logic, the feature store ensures that a feature used in training is computed identically when used in production inference — eliminating training-serving skew.

**Synonyms:** ML feature management, feature engineering platform, feature catalog, offline/online feature system.

**Example:** A banking fraud detection system stores transaction features (average amount, merchant category code, time since last transaction) in Feast — computed batch-wise in the offline store for model retraining and served with sub-millisecond latency from the Redis online store for real-time scoring.

---

## Model Registry

**Basic:** A model registry is a version control system for AI models — it tracks every version you've ever trained, what data and code produced it, and whether it's in testing or production.

**Intermediate:** A model registry is a centralized system for versioning, managing, and governing machine learning models throughout their lifecycle — from experimentation through staging to production deployment. It provides critical capabilities: automatic version tracking (each training run produces a new model version with a unique identifier), full lineage traceability (every model version is linked to the specific training run, dataset, code commit, and hyperparameters that produced it), stage transitions with approval gates (models move from "staging" to "production" only with proper authorization), metadata tagging and annotation (business context, performance metrics, known limitations), and canary/shadow deployment integration. The registry serves as the source of truth for answering questions like "Which model is currently serving production traffic?", "What training data was used for the fraud detection model deployed last Tuesday?", and "Can we reproduce the model that achieved 99.7% accuracy on the validation set?".

**Synonyms:** ML model catalog, model versioning system, model lifecycle manager, model governance registry.

**Example:** MLflow Model Registry tracks a credit risk model through versions 1 (staging), 2 (production — 99.2% accuracy), and 3 (archived — was recalled for data leakage), with each version linked to its training run, dataset hash, and deployment date.

---

## Shadow Deployment

**Basic:** Shadow deployment runs the new AI model silently alongside the old one — it sees the same traffic and produces answers, but nobody sees them. If the new model crashes, no one notices.

**Intermediate:** Shadow deployment is a zero-risk rollout strategy where a candidate model is deployed to production infrastructure and receives the same live traffic as the current production model, but its outputs are neither returned to users nor used for any business decisions. The shadow model's predictions are logged and analyzed offline to validate correctness, latency, resource consumption, and behavior before it is promoted to serve actual traffic. This approach is particularly valuable in AI systems because model behavior can be non-deterministic and context-dependent — a model that passes all offline evaluation tests may behave unexpectedly on real production traffic patterns. Shadow deployment allows the team to observe the model's behavior under true production conditions (data distribution, latency profile, memory footprint) without any risk to users or business operations. The technique is conceptually similar to canary deployment but with zero traffic exposure — the shadow model is a passive observer, not an active participant.

**Synonyms:** Shadow mode, dark launch, mirror deployment, passive evaluation.

**Example:** A bank deploys a new fraud detection model in shadow mode alongside the current model for one week — both models score every transaction, but only the current model's decisions block payments. Analysis reveals the new model catches 12% more fraud but also produces 3% more false positives, so it's sent back for threshold tuning before going live.

---

## Canary Deployment

**Basic:** Canary deployment gradually rolls out the new AI to a small percentage of users first — like testing a new recipe on 5% of customers before serving it to everyone.

**Intermediate:** Canary deployment is a controlled, incremental rollout strategy where a new model version is exposed to a small percentage of live traffic initially, with automated monitoring gates determining whether to gradually increase traffic or roll back. A typical canary progression might start at 5% of users for 30 minutes, then 25% for 2 hours, 50% for 4 hours, then full rollout if all metrics remain within acceptable bounds. The critical difference from shadow deployment is that canary traffic actually affects users — making the monitoring and rollback automation essential. Key metrics monitored during canary include: prediction accuracy (validated against outcomes that become known with a delay), latency percentiles (P50, P95, P99), error rates, business metrics (conversion rate, fraud detection rate), and resource utilization. Automated rollback triggers at the first sign of metric degradation. Canary deployment is the standard approach for production model updates in mature ML organizations.

**Synonyms:** Gradual rollout, traffic ramping, staged deployment, percentage-based deployment.

**Example:** A recommendation system deploys a new model version: 5% of users see recommendations from the new model for 1 hour (latency and click-through rate are within bounds), then 25% for 4 hours, 50% for 1 day, then 100%. At the 25% stage, the new model's P99 latency spikes to 2 seconds — automatic rollback to the previous version triggers within 30 seconds.

---

## Artifact Signing

**Basic:** Artifact signing is like putting a tamper-proof seal on your AI model or dataset so you can verify nobody swapped it out for a different one between training and deployment.

**Intermediate:** Artifact signing is the cryptographic practice of generating and verifying digital signatures for ML artifacts — models, datasets, containers, and evaluation results — throughout the CI/CD pipeline. Each artifact is hashed (typically SHA-256) and the hash is signed with a private key from a trusted hardware security module or key management service. The signature is stored alongside the artifact in the model registry and is verified at every stage transition: when a model moves from training to registry, from registry to staging, and from staging to production. This provides tamper-evident audit trails — any modification to a model binary, container image, or dataset is immediately detectable because the hash verification fails. Artifact signing is a foundational requirement for regulated industries (banking, pharma, government) where organizations must demonstrate that deployed models are exactly the versions that passed validation, with no unauthorized modifications in the deployment pipeline.

**Synonyms:** Model signing, cryptographic artifact verification, tamper-evident packaging, supply chain security.

**Example:** A pharma company signs its drug interaction model at training time. When the model is promoted to production, the deployment pipeline verifies the signature against the corporate signing key — if the model binary differs by even one byte, deployment is blocked and an alert fires.

---

## Data Contract

**Basic:** A data contract is a written agreement between the team that produces data and the team that uses it — specifying what the data looks like, how fresh it must be, and who fixes it when it breaks.

**Intermediate:** A data contract is a formal, machine-readable agreement between a data producer and one or more data consumers that defines the schema, semantics, quality SLAs, ownership, and lifecycle policies for a specific dataset. In the context of AI/ML pipelines, data contracts govern the inputs to feature engineering, model training, and inference. A well-defined data contract specifies: the exact schema (field names, types, constraints), freshness requirements (data must be no older than 15 minutes), completeness expectations (no more than 0.1% null values), ownership (who to notify on schema changes), and semantic definitions (what each field means, its business context). Data contracts are enforced programmatically through schema validation at pipeline ingress points — if a producer changes a column name from `customer_id` to `cust_id`, downstream pipelines fail with a clear error message rather than silently producing corrupted features. This practice is essential in AI-SDLC because model training and inference are exquisitely sensitive to data schema drift.

**Synonyms:** Data SLA, schema agreement, data governance contract, dataset specification.

**Example:** The "transaction_features" dataset contract specifies that `transaction_amount` is a non-nullable decimal(18,2), must be populated within 5 minutes of the transaction, any null values trigger a P1 alert to the payments team, and a 7-day notice is required before any schema change.

---

## Model Card

**Basic:** A model card is a nutrition label for an AI model — it tells you what the model was trained on, what it's good at, what it's bad at, and where it might be biased or dangerous.

**Intermediate:** A model card is a standardized transparency document that accompanies a machine learning model, providing structured information about its intended use, performance characteristics, evaluation results, limitations, and ethical considerations. Introduced by Mitchell et al. (2019), the model card framework addresses the fundamental information asymmetry between model developers and model consumers: without model cards, users deploy models without understanding their failure modes, biases, or appropriate use cases. A comprehensive model card includes: model details (architecture, version, training data), intended use (primary use cases, out-of-scope uses), factors (demographic or environmental factors that affect performance), metrics (overall and disaggregated performance across groups), evaluation data (datasets used for testing), ethical considerations (known biases, fairness analysis), and caveats (known limitations and recommendations). In regulated environments, model cards are increasingly mandatory as part of AI governance frameworks.

**Synonyms:** Model documentation, AI transparency report, model fact sheet, model datasheet.

**Example:** The model card for a credit scoring model reveals that while overall accuracy is 94%, accuracy drops to 72% for applicants under 25 with limited credit history — enabling risk-aware deployment decisions rather than blind trust in the aggregate metric.

---

## DORA Metrics

**Basic:** DORA metrics are four simple numbers that tell you how fast and how safely your engineering team delivers software — like a fitness tracker but for your development process.

**Intermediate:** DORA (DevOps Research and Assessment) metrics are four key performance indicators defined by Google's DORA research program that measure software delivery velocity and stability: Deployment Frequency (how often an organization successfully releases to production), Lead Time for Changes (the time from code commit to code successfully running in production), Mean Time to Recovery / MTTR (the time to restore service after an incident), and Change Failure Rate (the percentage of deployments causing a failure in production). In AI-augmented SDLC contexts, DORA metrics are particularly relevant because AI tools should demonstrably improve these metrics — and the metrics themselves must be measured with and without AI assistance to quantify the impact. The research consistently shows that elite performers (high DORA scores) are 4x more likely to have AI-assisted development practices. DORA metrics provide the empirical framework for measuring whether AI adoption is actually improving engineering outcomes.

**Synonyms:** Software delivery metrics, DevOps KPIs, delivery performance indicators, engineering velocity metrics.

**Example:** After implementing AI-assisted code review and automated test generation, a team's Lead Time for Changes decreased from 4 hours to 45 minutes and their Change Failure Rate dropped from 15% to 4%, moving them from "Medium" to "High" DORA performer category.

---

## ML Test Score

**Basic:** The ML Test Score is a checklist of 28 specific tests that tells you whether your AI system is truly ready for production — most teams score low and shouldn't deploy yet.

**Intermediate:** The ML Test Score is a rubric introduced by Google researchers Breck et al. (2017) consisting of 28 specific, actionable tests across four categories: Tests for Features and Data (data distribution checks, schema validation, feature importance monitoring), Tests for Model Development (model staleness checks, calibration evaluation, prediction bias testing), Tests for ML Infrastructure (pipeline reproducibility, compute resource monitoring, dependency versioning), and Tests for Monitoring (prediction drift detection, data quality alerts, model performance degradation detection). Each test is scored as "yes" (implemented), "no" (not implemented), or "not applicable." The total score provides a quantitative assessment of ML production readiness. The research found that most teams dramatically overestimate their production readiness — a score below 12 out of 28 indicates high risk of production failures. The ML Test Score provides a concrete, actionable framework for moving from experimental ML to production ML.

**Synonyms:** ML production readiness score, ML test rubric, ML maturity assessment, Breck rubric.

**Example:** A team preparing to deploy a customer churn prediction model runs the ML Test Score and scores 8/28 — they are missing data drift detection, automatic retraining triggers, and prediction monitoring. They add these before production, raising the score to 19/28.
