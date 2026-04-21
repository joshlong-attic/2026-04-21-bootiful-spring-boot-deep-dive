package com.example.agent;

import org.springaicommunity.agent.tools.SkillsTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@SpringBootApplication
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }

    @Bean
    QuestionAnswerAdvisor questionAnswerAdvisor(VectorStore vectorStore) {
        return QuestionAnswerAdvisor.builder(vectorStore).build();
    }

}

@Controller
@ResponseBody
class AdoptionsController {

    private final ChatClient ai;

    AdoptionsController(
            ToolCallbackProvider toolCallbackProvider ,
            QuestionAnswerAdvisor questionAnswerAdvisor,
            VectorStore vectorStore, DogRepository repository,
            ChatClient.Builder ai) {

        if (false) {
            repository.findAll().forEach(dog -> {
                var dogument = new Document("id: %s, name: %s, description: %s"
                        .formatted(dog.id(), dog.name(), dog.description()));
                vectorStore.add(List.of(dogument));
            });
        }
        var skills = SkillsTool
                .builder()
                .addSkillsResources(List.of(new ClassPathResource("/META-INF/skills")))
                .build();

        // skillsjars

        this.ai = ai
                .defaultAdvisors(questionAnswerAdvisor)
                .defaultToolCallbacks(toolCallbackProvider)
                .defaultToolCallbacks(skills)
                .defaultSystem("""
                        You are an AI powered assistant to help people adopt a dog from the adoptions agency named Pooch Palace with locations in Antwerp, Seoul, Tokyo, Singapore, Paris, Mumbai, New Delhi, Barcelona, San Francisco, and London. Information about the dogs availables will be presented below. If there is no information, then return a polite response suggesting we don't have any dogs available.
                        
                        If somebody asks you about animals, and there's no information in the context, then feel free to source the answer from other places.
                        
                        If somebody asks for a time to pick up the dog, don't ask other questions: simply provide a time by consulting the tools you have available.
                        
                        
                        """)
                .build();
    }

    @GetMapping("/ask")
    String ask(@RequestParam String question) {
        return this.ai
                .prompt()
                .user(question)
                .call()
                .content();
//                .entity(DogAdoptionSuggestion.class);

    }
}

record DogAdoptionSuggestion(int id, String name) {
}

record Dog(@Id int id, String name, String description) {
}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}