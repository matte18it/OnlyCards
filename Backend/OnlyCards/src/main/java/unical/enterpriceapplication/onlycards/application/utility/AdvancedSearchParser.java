package unical.enterpriceapplication.onlycards.application.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import unical.enterpriceapplication.onlycards.application.dto.AdvancedSearchDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@RequiredArgsConstructor
public class AdvancedSearchParser {
    // ----- METODI PARSING DI LISTE -----
    public List<AdvancedSearchDto> parsePokemonResponse(String jsonResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode dataNode = root.path("data");

        List<AdvancedSearchDto> resultList = new ArrayList<>();
        for (JsonNode cardNode : dataNode) {
            String id = cardNode.path("id").asText();
            String name = cardNode.path("name").asText();
            String image = cardNode.path("images").path("large").asText();
            String setName = cardNode.path("set").path("name").asText();
            String collectorNumber = cardNode.path("number").asText();
            String rarity = cardNode.path("rarity").asText();
            String type = cardNode.path("types").isArray() ?
                    StreamSupport.stream(cardNode.path("types").spliterator(), false)
                            .map(JsonNode::asText).collect(Collectors.joining(", ")) : "";

            AdvancedSearchDto advancedSearchDto = new AdvancedSearchDto();
            advancedSearchDto.setId(id);
            advancedSearchDto.setName(name);
            advancedSearchDto.setImage(image);
            advancedSearchDto.setSetName(setName);
            advancedSearchDto.setCollectorNumber(collectorNumber);
            advancedSearchDto.setRarity(rarity);
            advancedSearchDto.setType(type);

            resultList.add(advancedSearchDto);
        }
        return resultList;
    }   // metodo per il parsing della risposta di una richiesta di ricerca avanzata di carte Pokemon
    public List<AdvancedSearchDto> parseMagicResponse(String jsonResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode dataNode = root.path("data");

        List<AdvancedSearchDto> resultList = new ArrayList<>();
        for (JsonNode cardNode : dataNode) {
            String id = cardNode.path("id").asText();
            String setName = cardNode.path("set_name").asText();
            String collectorNumber = cardNode.path("collector_number").asText();
            String rarity = cardNode.path("rarity").asText();

            if (cardNode.has("card_faces")) {
                for (JsonNode faceNode : cardNode.path("card_faces")) {
                    String name = faceNode.path("name").asText();
                    String image = faceNode.path("image_uris").path("normal").asText();
                    String type = faceNode.path("type_line").asText();

                    AdvancedSearchDto advancedSearchDto = new AdvancedSearchDto();
                    advancedSearchDto.setId(id);
                    advancedSearchDto.setName(name);
                    advancedSearchDto.setImage(image);
                    advancedSearchDto.setSetName(setName);
                    advancedSearchDto.setCollectorNumber(collectorNumber);
                    advancedSearchDto.setRarity(rarity);
                    advancedSearchDto.setType(type);

                    resultList.add(advancedSearchDto);
                }
            } else {
                String name = cardNode.path("name").asText();
                String image = cardNode.path("image_uris").path("normal").asText();
                String type = cardNode.path("type_line").asText();

                AdvancedSearchDto advancedSearchDto = new AdvancedSearchDto();
                advancedSearchDto.setId(id);
                advancedSearchDto.setName(name);
                advancedSearchDto.setImage(image);
                advancedSearchDto.setSetName(setName);
                advancedSearchDto.setCollectorNumber(collectorNumber);
                advancedSearchDto.setRarity(rarity);
                advancedSearchDto.setType(type);

                resultList.add(advancedSearchDto);
            }
        }
        return resultList;
    }   // metodo per il parsing della risposta di una richiesta di ricerca avanzata di carte Magic

    // ----- METODI PARSING DI UN SINGOLO OGGETTO -----
    public AdvancedSearchDto parseSinglePokemonResponse(String jsonResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode cardNode = root.path("data"); // Seleziona il primo oggetto per singolo parsing

        String id = cardNode.path("id").asText();
        String name = cardNode.path("name").asText();
        String image = cardNode.path("images").path("large").asText();
        String setName = cardNode.path("set").path("name").asText();
        String collectorNumber = cardNode.path("number").asText();
        String rarity = cardNode.path("rarity").asText();
        String type = cardNode.path("types").isArray() ?
                StreamSupport.stream(cardNode.path("types").spliterator(), false)
                        .map(JsonNode::asText).collect(Collectors.joining(", ")) : "";

        AdvancedSearchDto advancedSearchDto = new AdvancedSearchDto();
        advancedSearchDto.setId(id);
        advancedSearchDto.setName(name);
        advancedSearchDto.setImage(image);
        advancedSearchDto.setSetName(setName);
        advancedSearchDto.setCollectorNumber(collectorNumber);
        advancedSearchDto.setRarity(rarity);
        advancedSearchDto.setType(type);

        return advancedSearchDto;
    }   // metodo per il parsing della risposta di una richiesta di ricerca avanzata di carte Pokemon
    public AdvancedSearchDto parseSingleMagicResponse(String jsonResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResponse);

        String id = root.path("id").asText();
        String setName = root.path("set_name").asText();
        String collectorNumber = root.path("collector_number").asText();
        String rarity = root.path("rarity").asText();

        String name, image, type;

        if (root.has("card_faces")) {
            JsonNode faceNode = root.path("card_faces").get(0);
            name = faceNode.path("name").asText();
            image = faceNode.path("image_uris").path("normal").asText();
            type = faceNode.path("type_line").asText();
        } else {
            name = root.path("name").asText();
            image = root.path("image_uris").path("normal").asText();
            type = root.path("type_line").asText();
        }

        AdvancedSearchDto advancedSearchDto = new AdvancedSearchDto();
        advancedSearchDto.setId(id);
        advancedSearchDto.setName(name);
        advancedSearchDto.setImage(image);
        advancedSearchDto.setSetName(setName);
        advancedSearchDto.setCollectorNumber(collectorNumber);
        advancedSearchDto.setRarity(rarity);
        advancedSearchDto.setType(type);

        return advancedSearchDto;
    }   // metodo per il parsing della risposta di una richiesta di ricerca avanzata di carte Magic

    // ----- METODI PARSING COMUNI -----
    public AdvancedSearchDto parseYuGiOhResponse(String jsonResponse) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(jsonResponse);
        JsonNode cardNode = root.path("data").get(0); // Seleziona il primo oggetto per singolo parsing

        String id = cardNode.path("id").asText();
        String name = cardNode.path("name").asText();
        String type = cardNode.path("type").asText();

        JsonNode firstSet = cardNode.path("card_sets").get(0);
        String setName = firstSet.path("set_name").asText();
        String collectorNumber = firstSet.path("set_code").asText();
        String rarity = firstSet.path("set_rarity").asText();

        String image = cardNode.path("card_images").get(0).path("image_url").asText();

        AdvancedSearchDto advancedSearchDto = new AdvancedSearchDto();
        advancedSearchDto.setId(id);
        advancedSearchDto.setName(name);
        advancedSearchDto.setImage(image);
        advancedSearchDto.setSetName(setName);
        advancedSearchDto.setCollectorNumber(collectorNumber);
        advancedSearchDto.setRarity(rarity);
        advancedSearchDto.setType(type);

        return advancedSearchDto;
    }   // metodo per il parsing della risposta di una richiesta di ricerca avanzata di carte Yu-Gi-Oh
}
