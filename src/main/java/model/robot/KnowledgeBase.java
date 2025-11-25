package model.robot;

import lombok.Data;
import java.util.ArrayList;

/**
 * Represents TerraBot's knowledge base of learned facts
 */
@Data
public class KnowledgeBase {
    /**
     * List of topics (component names)
     */
    private ArrayList<String> topics;

    /**
     * List of lists: facts.get(i) contains all facts for topics.get(i)
     */
    private ArrayList<ArrayList<String>> facts;

    public KnowledgeBase() {
        this.topics = new ArrayList<>();
        this.facts = new ArrayList<>();
    }

    /**
     * Adds a fact to the knowledge base under the given topic
     *
     * @param topic the topic (component name)
     * @param fact  the fact (subject) to add
     */
    public void addFact(final String topic, final String fact) {
        // Find if topic already exists
        int topicIndex = -1;
        for (int i = 0; i < topics.size(); i++) {
            if (topics.get(i).equals(topic)) {
                topicIndex = i;
                break;
            }
        }

        // If topic doesn't exist, create it
        if (topicIndex == -1) {
            topics.add(topic);
            facts.add(new ArrayList<>());
            topicIndex = topics.size() - 1;
        }

        // Add fact to the topic's list
        facts.get(topicIndex).add(fact);
    }

    /**
     * Gets all facts for a given topic
     *
     * @param topic the topic
     * @return list of facts, or empty list if topic doesn't exist
     */
    public ArrayList<String> getFacts(final String topic) {
        for (int i = 0; i < topics.size(); i++) {
            if (topics.get(i).equals(topic)) {
                return facts.get(i);
            }
        }
        return new ArrayList<>();
    }
}
