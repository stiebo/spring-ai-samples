# Spring AI Utilities Demo

This demo showcases Spring AI to integrate advanced AI capabilities, such as vector embeddings and similarity search, into document processing and conversational AI.

## Analyze CV Endpoint

The **Analyze CV** endpoint allows users to upload a CV in PDF format and receive a detailed breakdown of its content in JSON format. This helps in extracting key information from CVs in a structured way, enabling easier analysis and integration with other systems. This functionality leverages the [structured output capabilities in Spring AI](https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html) to format and present the extracted data.

## ChatWithMyDocuments Endpoints

The **ChatWithMyDocuments** endpoints and service facilitate interaction with a repository of PDF documents, allowing users to upload, list, delete, and chat about document contents.&#x20;

The service layer of this implementation leverages advanced technologies like [Retrieval Augmented Generation (RAG)](https://docs.spring.io/spring-ai/reference/api/vectordbs.html) in Spring AI, including the creation of vector embeddings using an OpenAI model. When a document is added, its content is processed and transformed into vectors through OpenAI embedding model calls that capture semantic meaning, which are then stored in a PGVectorStore for efficient retrieval. This allows for effective similarity searches later on.

When a user interacts through the chat endpoint, the pre-stuffed "QuestionAnswerAdvisor" utilizes similarity search to find the most relevant document content and incorporate it into the prompt (Retrieval Augmented Generation, RAG), providing accurate and contextually enriched responses.

The entire solution is built using Spring AI, enabling seamless integration of AI capabilities into the service layer in just a few lines of code.

The implementation utilizes AI-backed processing to read, split, and store vector representations of documents, facilitating seamless question-answer interactions using the chat client.

Endpoints:

- **Add Document**: Allows users to upload a PDF file to the repository, ensuring no duplicates by checking existing file names in the database.
- **List Documents**: Retrieves a list of all available documents in the repository.
- **Delete Document**: Deletes a document from the repository by specifying the document's name.
- **Chat with Documents**: Provides a conversational interface for users to ask questions and receive relevant information based on the document content.

## Flashcard Generation Endpoint

The **Create Flashcards** endpoint allows users to generate study flashcards from any uploaded **image** or (text-based) PDF files.

It demonstrates [Spring AI multimodality](https://docs.spring.io/spring-ai/reference/api/multimodality.html), utilizing the LLMs feature to process and generate text in conjunction with other modalities such an image.

By leveraging AI, this endpoint processes the uploaded content to extract key information and transform it into interactive flashcards in JSON format, which can be used for studying or review. The functionality supports efficient learning and study practices by utilizing AI to identify relevant concepts and generate useful questions and answers in the form of flashcards.



