package simulation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fileio.CommandInput;
import fileio.SimulationInput;

import java.util.List;

/**
 * Coordinates multiple simulations and command execution
 */
public class SimulationManager {

    /**
     * All simulations described in the input file
     */
    private final List<SimulationInput> simulations;

    /**
     * Global command list that drives all simulations
     */
    private final List<CommandInput> commands;

    /**
     * Jackson mapper used to construct JSON output nodes
     */
    private final ObjectMapper mapper;

    /**
     * Factory responsible for building {@link Simulation} instances
     * from {@link SimulationInput} data
     */
    private final SimulationFactory simulationFactory;

    /**
     * Constructs a manager for all simulations defined in the input
     *
     * @param simulations the simulation parameter list
     * @param commands    the global command list
     * @param mapper      the Jackson mapper used to build JSON output
     */
    public SimulationManager(final List<SimulationInput> simulations,
                             final List<CommandInput> commands,
                             final ObjectMapper mapper) {
        this.simulations = simulations;
        this.commands = commands;
        this.mapper = mapper;
        this.simulationFactory = new SimulationFactory();
    }

    /**
     * Runs all simulations and returns the aggregated JSON output
     *
     * @return an ArrayNode representing the checker output
     */
    public ArrayNode runAll() {
        ArrayNode output = mapper.createArrayNode();
        executeAllCommands(output);
        return output;
    }

    /**
     * Executes all commands for all simulations and fills the provided output node
     *
     * @param output the output array node to be populated
     */
    private void executeAllCommands(final ArrayNode output) {
        Simulation currentSimulation = null;
        int currentSimulationIndex = 0;

        for (CommandInput cmd : commands) {
            String name  = cmd.getCommand();

            switch (name) {
                case "startSimulation" -> {
                    if (currentSimulation != null) {
                        ObjectNode error = mapper.createObjectNode();
                        error.put("command", name);
                        error.put("message",
                                "ERROR: Simulation already started. Cannot perform action");
                        error.put("timestamp", cmd.getTimestamp());

                        output.add(error);
                        continue;
                    }
                    SimulationInput simInput = simulations.get(currentSimulationIndex++);
                    currentSimulation = simulationFactory.build(simInput);

                    executeCommand(currentSimulation, cmd, output);
                }
                case "endSimulation" -> {
                    executeCommand(currentSimulation, cmd, output);
                    currentSimulation = null;
                }
                default -> executeCommand(currentSimulation, cmd, output);
            }
        }
    }

    /**
     * Executes a single command within the context of the given simulation
     *
     * @param simulation current simulation instance
     * @param command    command to execute
     * @param output     output array to add result nodes to
     */
    private void executeCommand(final Simulation simulation,
                                final CommandInput command,
                                final ArrayNode output) {
        if (simulation == null) {
            ObjectNode error = mapper.createObjectNode();
            error.put("command", command.getCommand());
            error.put("message", "ERROR: Simulation not started. Cannot perform action");
            error.put("timestamp", command.getTimestamp());

            output.add(error);
            return;
        }

        simulation.executeCommand(command, output, mapper);
    }
}



