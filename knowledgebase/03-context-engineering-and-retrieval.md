# Group 3: Context Engineering & Retrieval

---

## Context Engineering

**Basic:** Context engineering gathers up all the relevant files, definitions, and project information around you, summarizes them neatly, and feeds them to the AI so it understands the big picture of your app.

**Intermediate:** Context engineering is the algorithmic process of assembling, pruning, formatting, and prioritizing relevant repository information into a structured token stream that maximizes the model's environment legibility. This is far more sophisticated than simply concatenating files — it involves converting complex, multi-dimensional workspace structures — including dependency graphs, symbol tables, active file states, recent git history, compiler diagnostics, and architectural boundaries — into a linear token budget that fits within the model's context window. The engineer must make strategic trade-offs: which files provide the most signal per token? Should the full dependency graph be included or just the direct imports? How much of the test file should accompany a production code change request? Effective context engineering multiplies the effective capability of the model far more than upgrading to a larger model would. Common techniques include: symbol graph extraction (only including function signatures and type definitions), dependency-aware file selection (automatically including imported modules), and diff-aware context (only showing changed lines plus surrounding context).

**Synonyms:** Prompt assembly, workspace hydration, context construction, environment legibility engineering, retrieval-augmented hydration.

**Example:** When a developer asks an agent to "add input validation to the registration endpoint", the context engineering pipeline selects: the route handler file, the request DTO definition, the existing validation utility file, and the database schema — but excludes unrelated files like deployment configs and test fixtures.

---

## RAG (Retrieval-Augmented Generation)

**Basic:** RAG lets the AI look up relevant information from your project's knowledge base or documentation before answering, instead of just guessing from its training data.

**Intermediate:** Retrieval-Augmented Generation is a pattern where the model's input is augmented with relevant context retrieved from an external knowledge source at inference time. Before the model generates a response, the system performs a semantic search over a pre-indexed knowledge base (documentation, codebase embeddings, architectural decision records, past conversations) to find the most relevant chunks. These chunks are injected into the context window alongside the original query, grounding the model's generation in factual, project-specific information rather than relying solely on its parametric memory (which may be outdated, incorrect, or too generic). RAG is the most widely adopted mitigation for hallucination in production systems because it provides the model with authoritative source material it can reference directly. The effectiveness of RAG depends critically on three factors: retrieval quality (finding truly relevant chunks), chunk quality (properly segmented, self-contained pieces of information), and context window management (not crowding out the primary task with too many retrieved chunks).

**Synonyms:** Grounded generation, knowledge-augmented generation, semantic retrieval, retrieval-augmented prompting.

**Example:** A developer asks "How do we handle authentication in this project?" — the RAG system retrieves the relevant sections from the project's ADR documents and the auth middleware source code, injects them into the prompt, and the model generates an answer grounded in the actual project implementation rather than generic OAuth knowledge.

---

## Vector Database

**Basic:** A vector database is a search engine that finds things by meaning instead of by keywords — you search "how to connect to database" and it finds the right code example even if those exact words aren't there.

**Intermediate:** A vector database is a specialized index and query engine designed for storing and searching high-dimensional embedding vectors at scale. Unlike traditional databases that use exact keyword matching or structured queries, vector databases organize entries by their semantic proximity in the embedding space. When a query arrives, it is first converted to an embedding vector by the same embedding model used to index the data, then the database searches for the nearest neighbors using similarity metrics (cosine similarity, dot product, or L2 distance). Modern vector databases implement Approximate Nearest Neighbor (ANN) algorithms — HNSW (Hierarchical Navigable Small World), IVF (Inverted File Index), or PQ (Product Quantization) — that dramatically accelerate search at the cost of minor accuracy loss. Key operational concerns include: index build time, query latency at scale, recall accuracy, filtering capabilities (metadata pre-filtering or post-filtering), and storage costs. In AI-SDLC contexts, vector databases typically index code snippets, documentation, architectural decision records, and historical conversation trajectories.

**Synonyms:** Vector index, embedding store, semantic search engine, similarity search database, ANN index.

**Example:** A team vectors their entire internal library documentation into a vector database. When a developer asks "How do I publish a message to the event bus?", the database finds the relevant code snippet from the event library docs even though neither "publish" nor "event bus" existed in the query's exact wording.

---

## Chunk Size & Overlap

**Basic:** Chunk size controls how big each piece of code you feed to the AI is — too big and it wastes space, too small and it loses context. Overlap makes sure no important information falls through the cracks between chunks.

**Intermediate:** Chunk size and overlap define the strategic windowing parameters used to segment source code, documentation, or other long-form content into discrete blocks suitable for embedding and retrieval. The chunk size determines the maximum token count of each segment before it is independently embedded and indexed. Optimal chunk size involves a fundamental trade-off: smaller chunks (128-256 tokens) provide more precise retrieval but may lack the surrounding context needed to understand the code, while larger chunks (512-1024 tokens) provide richer context but capture more irrelevant information, reducing retrieval precision. Overlap specifies the number of tokens shared between adjacent chunks to maintain continuity across boundaries — this is critical for code because a function definition could start at the end of chunk N and continue at the start of chunk N+1. The most effective code chunking strategies are AST-aware: they use the language's abstract syntax tree to identify natural boundaries (function declarations, class definitions, import blocks) and split at those points rather than at arbitrary token counts.

**Synonyms:** Text segmentation windows, token windowing, splitting threshold, AST-aware chunking, document partitioning.

**Example:** A codebase processor is configured with a chunk size of 512 tokens and overlap of 64 tokens using AST-aware splitting — this ensures that a 600-token function is contained in a single chunk rather than being severed mid-body.

---

## Similarity Metric

**Basic:** Similarity metric is the specific math formula used to measure how close two pieces of meaning are on the AI's map — like measuring distance in kilometers vs. miles but for meaning instead of geography.

**Intermediate:** A similarity metric is the mathematical formulation used to evaluate the proximity between a query embedding vector and a stored document or code embedding vector within a latent vector space. The choice of metric must match the training objective of the embedding model — using the wrong metric will produce systematically degraded retrieval results. The three dominant metrics are: Cosine Similarity (measures the angle between vectors, ignoring magnitude — most common for text embeddings), Dot Product (measures both angle and magnitude — used when vector length carries semantic information, common in certain embedding models like OpenAI's text-embedding-ada), and Euclidean/L2 Distance (measures the straight-line distance — sensitive to both direction and magnitude, used when the embedding space is trained with contrastive loss). In practice, cosine similarity is the default choice for most text and code retrieval systems because it normalizes for vector length, which tends to correlate with document length rather than semantic content.

**Synonyms:** Vector distance function, proximity measure, similarity function, retrieval metric, embedding distance formula.

**Example:** A retrieval pipeline uses cosine similarity — when the query embedding vector for "sorting algorithm" is compared to stored chunk vectors, the chunk containing a quicksort implementation scores 0.94 while an unrelated chunk about UI layout scores 0.12.

---

## Embedding Model

**Basic:** An embedding model is a specialized AI that reads text and turns it into a set of coordinates on a meaning map — it's smaller and faster than a full language model because it only needs to understand, not generate.

**Intermediate:** An embedding model is a specialized neural network — smaller, faster, and cheaper than a full generative language model — optimized specifically for converting text or code into dense vector representations that capture semantic and functional meaning. Unlike generative models that predict the next token, embedding models are trained with contrastive or representation learning objectives: they learn to position semantically similar texts close together in vector space and dissimilar texts far apart. These models are typically based on the Transformer encoder architecture (like BERT) or decoder architecture with pooling layers, and they produce a single fixed-size vector for an entire input sequence rather than a sequence of tokens. Popular embedding models (text-embedding-ada-002, text-embedding-3-small, sentence-transformers) range from 384 to 3072 dimensions, with larger dimensions providing higher precision at greater storage and compute cost. The embedding model is the critical infrastructure component that determines retrieval quality — a better embedding model improves every downstream system that depends on semantic search.

**Synonyms:** Encoder model, representation model, semantic encoder, bi-encoder, embedding encoder.

**Example:** The `text-embedding-3-small` model converts the sentence "Refactor the authentication module to use JWT tokens" into a 1536-dimensional vector, which is then stored in a vector database alongside thousands of other code-related embeddings for fast retrieval.

---

## Token Budget

**Basic:** Token budget is planning how much of the AI's limited attention span each part of the conversation gets — like deciding how many words your system instructions deserve vs. how many the actual code gets.

**Intermediate:** Token budget is the strategic allocation of the model's finite context window across the competing demands of system prompts, retrieved context, conversation history, tool call results, and the current user request. Every token in the context window is a precious resource — exceeding the limit causes truncation (typically from the middle, where context is already least effective), while poor allocation wastes capacity on low-value information. A typical budget for a 128K context might allocate: 4K for system prompt and AGENTS.md, 16K for retrieved context (RAG results + relevant code snippets), 8K for conversation history (compressed to retain key decisions), 4K for tool call schemas and recent tool results, and the remainder for the current task. Budget management becomes an active optimization problem in long-running agent sessions where the conversation history grows continuously — the harness must implement compression strategies (summarizing old turns), pruning strategies (removing irrelevant branches), and sliding window approaches (keeping only the N most recent turns).

**Synonyms:** Context allocation, token management, prompt budget, context window planning, token economy.

**Example:** An OpenCode agent managing a complex refactoring task across 15 agent turns allocates: system prompt (3K tokens), AGENTS.md rules (1K), retrieved code context (12K), conversation summary (2K), tool schemas (2K), and current task payload (8K) — staying within the 128K limit by compressing earlier turns into a summary.

---

## Prefix Caching

**Basic:** Prefix caching remembers text the AI has already read so it doesn't have to waste time re-reading the same instructions every time you start a new task — like bookmarking a page instead of reading the whole book again.

**Intermediate:** Prefix caching is an optimization technique that reuses the pre-computed Key-Value cache from a shared initial token sequence across multiple inference calls, dramatically reducing Time-To-First-Token (TTFT) and compute cost. When identical leading token sequences (the prefix) appear across multiple requests — such as a long system prompt, AGENTS.md contents, or a library of tool schemas — the KV cache for these tokens can be computed once and reused for every subsequent interaction. Modern inference providers and inference engines implement prefix caching transparently, detecting identical prefix sequences by hash or by explicit prefix markers. The practical impact is substantial: a 10K token system prompt that would take 500ms to prefill on every request can be reduced to near-zero prefill time when the KV cache is reused. However, prefix caching has memory implications — each cached prefix consumes GPU memory proportional to its length and the model's hidden dimension, so cache eviction policies (LRU, TTL) are necessary in high-throughput environments.

**Synonyms:** KV cache reuse, prompt caching, prefix KV cache, attention cache sharing.

**Example:** All agent sessions in an organization share an identical 8K-token system prompt containing company coding standards and security policies — the inference provider caches the KV state for this prefix once and serves 400+ developer sessions from the cached state, reducing each session's TTFT by 300ms.
