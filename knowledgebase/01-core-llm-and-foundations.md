# Group 1: Core LLM & Foundation Concepts

---

## Token

**Basic:** A token is a tiny piece of text — like a word fragment or punctuation mark — that the AI reads one by one, the same way you read letter by letter.

**Intermediate:** A token is the minimal atomic unit of data processed by a large language model. Raw text is never fed directly into a neural network; instead, a tokenizer (typically Byte-Pair Encoding) deterministically splits the input string into sub-word units — individual words, word fragments like "ing" or "ed", whitespace characters, or syntax symbols like brackets and semicolons. Each of these fragments is mapped to a unique integer index from the model's fixed vocabulary. Models operate entirely on these integer sequences; they have no concept of characters, letters, or raw strings. The vocabulary size (typically 32K–128K tokens) and the tokenization algorithm directly impact how efficiently a model can process a given language or programming language.

**Synonyms:** Sub-word units, vocabulary indices, tokenized chunks, BPE fragments.

**Example:** The Python code `def calculate(a, b):` is tokenized into fragments like `["def", " calculate", "(", "a", ",", " b", "):"]` before the model processes it.

---

## Embedding

**Basic:** An embedding is like a set of GPS coordinates that tells the AI where a word's meaning is located on a giant map — words with similar meanings are parked close together.

**Intermediate:** An embedding is a dense, continuous vector of floating-point numbers (typically 768–4096 dimensions) that maps a token, code block, or document into a high-dimensional latent space. Unlike sparse one-hot encodings, embeddings capture semantic and functional relationships: the model learns during pre-training to position tokens so that similar concepts (e.g., "ArrayList" and "LinkedList", or "if" and "else") have small vector distances between them. This geometric arrangement is what enables the model to generalize — it has learned not just the word itself but its role, context, and relationships to every other token in the vocabulary. Embeddings are computed by the model's embedding layer (the first transformation applied to input tokens) and flow through every subsequent attention and feed-forward layer.

**Synonyms:** Latent space vectors, dense feature vectors, semantic coordinates, hidden state representations.

**Example:** In a vector database storing code documentation, the embeddings for "How to connect to a PostgreSQL database" and "PostgreSQL connection string example" sit close together in the vector space, making them both retrievable from either query.

---

## Model Weights

**Basic:** Model weights are billions of tiny adjustable knobs inside the AI's brain that were tuned during training to make sure it gives the right answers.

**Intermediate:** Model weights are the learnable numerical parameters (denoted as W in neural network notation) that constitute the bulk of a language model's knowledge and behavior. These are floating-point matrices distributed across the Transformer's layers — self-attention projections (Q, K, V, O), feed-forward network gates and up/down projections, layer normalization scales and biases, and the embedding and un-embedding matrices. During pre-training, backpropagation updates these weights to minimize the next-token prediction loss across trillions of text tokens. Once training is complete, the weights are frozen and loaded into high-bandwidth memory (HBM) on inference hardware. They act as a static, massive associative memory: the model's entire knowledge of language, syntax, code patterns, facts, and reasoning is encoded in these numerical matrices. Weight size (measured in parameters) directly correlates with model capability and memory requirements — a 70B-parameter model requires roughly 140GB of memory at 16-bit precision.

**Synonyms:** Parametric coefficients, neural network parameters, synaptic weights, core learnable matrices.

**Example:** GPT 5.4 mini's model weights, stored across dozens of gigabytes of HBM, determine how the model transforms the input prompt "Write a Python function that sorts a list" into a probability distribution over the next token.

---

## Transformer

**Basic:** The Transformer is the architectural blueprint that all modern AI models use — it's a multi-layered grid of math operations that reads all input text at once rather than word by word.

**Intermediate:** The Transformer is the foundational deep-learning architecture introduced in the 2017 paper "Attention Is All You Need" that underlies virtually every modern large language model. Unlike recurrent networks that process sequences step by step, the Transformer processes all tokens in parallel through stacked layers of two primary sub-components: Multi-Head Self-Attention and Feed-Forward Networks (FFN). The attention mechanism computes weighted relationships between every pair of tokens in the context window, allowing the model to dynamically focus on relevant information regardless of position. Multiple attention heads (typically 32–96) run in parallel, each capturing different types of relationships (syntax, semantics, coreference, etc.). The FFN layers that follow each attention block perform learned non-linear transformations on each token's representation independently. Stacking these blocks (typically 32–96 layers deep) creates a hierarchical representation where lower layers capture syntax and surface patterns while deeper layers encode abstract reasoning and task-specific behaviors.

**Synonyms:** Transformer architecture, attention-based neural network, decoder-only stack, encoder-decoder topology.

**Example:** When a developer pastes a full class definition into GitHub Copilot, the Transformer processes every token simultaneously through its stacked attention and feed-forward layers to generate context-aware completions.

---

## Tensor Shape

**Basic:** Tensor shape describes the exact column and row dimensions of the data grids inside the AI — it determines how much information can be processed in each step.

**Intermediate:** Tensor shape defines the precise dimensional layout of all data flowing through a Transformer model — both the activations (data) and the weights (parameters). Every operation in the model is a tensor operation: matrix multiplication, reshaping, transposition, or reduction. Key shape parameters include: the hidden dimension (d_model, typically 4096–8192), which determines the width of each token's representation; the number of attention heads (n_heads, typically 16–96), which divides the hidden dimension into parallel subspaces; the number of layers (n_layers, typically 32–96), defining the model's depth; and the vocabulary size (vocab_size, typically 32K–128K), defining the output dimension. These shape parameters are the primary architectural decisions that determine a model's total parameter count, its memory footprint at inference, its flops-per-token efficiency, and its effective capacity for learning complex patterns.

**Synonyms:** Network dimensionality, model layout, tensor network architecture, hidden state dimensions.

**Example:** GPT 5.4 mini's tensor shape of 4096 hidden dimensions across 32 layers with 32 attention heads determines that each token gets a 4096-number representation while the model processes 32 different relationship types in parallel.

---

## Context Window

**Basic:** The context window is the AI's short-term memory limit — it's exactly how much text the AI can look at in one go, like reading a single page instead of the whole book.

**Intermediate:** The context window defines the maximum number of tokens that a Transformer model can attend to in a single forward pass — the total input capacity including system prompts, user messages, retrieved context, conversation history, and the current request. This limit is fundamentally constrained by the quadratic memory complexity of standard softmax attention: doubling the context length quadruples the memory required for attention scores (O(n²)). Models with 4K, 8K, 32K, 128K, or even 1M token windows exist, but longer windows typically require architectural innovations — sparse attention patterns, sliding window attention, or linear attention variants — to manage memory costs. The effective usable window is often smaller than the theoretical maximum because attention degradation causes the model to lose precision on information in the middle of the context (the "lost in the middle" effect), making prompt engineering and token budgeting critical practical skills.

**Synonyms:** Attention horizon, token capacity, input buffer, context length, sequence length.

**Example:** When an OpenCode pipeline processes a monolithic 10,000-line legacy Java class, it must strategically select which portions fit within GPT 5.4 mini's 128K token window to avoid attention degradation on critical method signatures in the middle of the payload.

---

## Inference

**Basic:** Inference is when the AI actually does its math to think up and type out an answer for you — it's the live running of the model.

**Intermediate:** Inference is the compute execution phase of a language model, characterized by two distinct operational stages. The prefill phase processes all prompt tokens simultaneously in parallel, computing their hidden states and constructing the Key-Value (KV) cache — this is compute-bound and highly parallelizable. The decoding phase then generates tokens one at a time auto-regressively: for each new token, the model runs a forward pass using the KV cache (avoiding recomputation of previous token attention), producing a probability distribution over the vocabulary from which the next token is sampled. This phase is memory-bandwidth-bound because the rate-limiting step is loading model weights from HBM into compute units. The ratio of prefill to decode time depends on prompt length and generation length. Inference latency is typically measured as Time-To-First-Token (TTFT, dominated by prefill) and tokens-per-second (dominated by decode bandwidth).

**Synonyms:** Forward pass, token generation, model invocation, generation cycle, serving.

**Example:** GitHub Copilot runs inference on specialized GPU clusters: the prefill phase processes your current file context in milliseconds, then the decoding phase streams code completions back at roughly 50-100 tokens per second.

---

## KV Cache

**Basic:** The KV cache is the AI's scratchpad — it remembers what it already read earlier in the conversation so it doesn't have to re-read everything from scratch when generating each new word.

**Intermediate:** The Key-Value (KV) cache is a memory structure that stores the Key and Value projections from each attention layer's computation for every token processed so far. During the decoding phase, each new token needs to attend to all previous tokens; without the KV cache, the model would recompute all previous token representations from scratch for each new token — an O(n²) cost that would make autoregressive generation prohibitively slow. By caching the K and V matrices from the prefill phase and appending new entries as each token is generated, the model reduces per-step computation to O(n) for the attention operation. The KV cache is typically the largest memory consumer during inference: for a 70B model with 4096 hidden dimension, 80 layers, and 4096 tokens of context, the KV cache alone consumes approximately 40GB of HBM at 16-bit precision.

**Synonyms:** Attention cache, prefix cache, precomputed key-value store.

**Example:** When an AI agent engages in a 20-turn debugging conversation, the KV cache accumulates the Key-Value projections from every previous turn, allowing each new response to attend to the full history without recomputing past attention scores.

---

## Temperature

**Basic:** Temperature controls how creative or predictable the AI is — low temperature gives the same safe answer every time, high temperature produces surprising and varied responses.

**Intermediate:** Temperature is a sampling hyperparameter that scales the logits (raw prediction scores) before the softmax function converts them into a probability distribution over the vocabulary. It controls the sharpness of the distribution: low temperature values (0.0–0.5) amplify the probability gap between the most likely token and alternatives, producing deterministic, repetitive, and conservative outputs. High temperature values (0.8–2.0) flatten the distribution, making less likely tokens more probable, which increases creativity and diversity but also introduces randomness and potential incoherence. At temperature 0, the model becomes deterministic (greedy decoding — always picks the highest-probability token). In software engineering contexts, low temperature (0.0–0.2) is typically preferred for code generation (correctness matters), while higher temperatures may be useful for test case generation, documentation writing, or exploratory design discussions.

**Synonyms:** Sampling temperature, logit scaling, creativity parameter, randomness control.

**Example:** A code generation agent sets temperature to 0.1 when generating production code (preferring correct, idiomatic patterns) but raises it to 0.8 when generating unit test variations (needing diverse edge case exploration).

---

## Hallucination

**Basic:** Hallucination is when the AI confidently makes things up that aren't true — like inventing a function name that doesn't exist or citing a book that was never written.

**Intermediate:** Hallucination refers to the phenomenon where a language model generates factually incorrect, nonsensical, or unverifiable content while maintaining a confident and plausible tone. This is not a bug in the traditional sense — it is a fundamental and unavoidable characteristic of autoregressive language models. The model is trained to maximize the probability of the next token given the preceding context; it has no internal representation of "truth" or "factuality," only statistical patterns learned from training data. Hallucinations manifest in several forms: fact-confabulation (asserting false information), source-confabulation (inventing citations or function names), instruction-override (ignoring context constraints), and logic-confabulation (producing contradictory reasoning). In code generation specifically, hallucinations produce plausible-looking code that uses nonexistent APIs, invents library functions, or constructs logically consistent but functionally incorrect algorithms. Mitigation strategies include RAG, constraint injection, multi-agent verification loops, and evaluation harnesses.

**Synonyms:** Confabulation, model invention, factual fabrication, plausible nonsense.

**Example:** An LLM asked to generate a pandas transformation invents a method called `DataFrame.merge_conditional()` that sounds plausible, follows pandas naming conventions, but does not exist in any version of the library.

---

## System Prompt

**Basic:** The system prompt is the instruction manual you give the AI before it starts working — it tells the AI who it is, what rules to follow, and how to behave.

**Intermediate:** The system prompt is a special pre-pended message in the model's context that establishes persistent behavioral constraints, role definitions, and operational boundaries for the duration of a conversation or task. Unlike user messages which change with each interaction, the system prompt remains fixed (or updates only at defined boundaries) and occupies the most influential position in the context — it is the first content the model processes during the prefill phase. The harness injects the system prompt before any user input, typically containing: role definition ("You are an expert Python developer"), behavioral constraints ("Always ask for clarification before writing more than 100 lines"), tool availability descriptions, output format specifications (JSON schema, markdown structure), security policies ("Never execute shell commands that modify files"), and repository-specific context (AGENTS.md contents, coding standards, architectural rules). Effective system prompt engineering is one of the highest-leverage optimization points in agentic systems.

**Synonyms:** Instruction prefix, role prompt, system message, meta-instruction, behavioral manifest.

**Example:** An OpenCode AGENTS.md file configures the system prompt to define the agent as "an expert in Rust and TypeScript, always runs cargo check before presenting code, and never proposes solutions that modify the CI/CD pipeline without explicit user approval."
