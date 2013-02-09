package cpsc470.pursuit;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.Timer;

import aima.core.agent.Action;
import aima.core.agent.Percept;
import aima.core.util.datastructure.XYLocation;

import cpsc470.pursuit.agent.AStarPursuedAgent;
import cpsc470.pursuit.agent.NoOpPursuedAgent;
import cpsc470.pursuit.agent.NoOpPursuerAgent;
import cpsc470.pursuit.agent.OnlineGreedyPursuedAgent;
import cpsc470.pursuit.agent.OnlineGreedyPursuerAgent;
import cpsc470.pursuit.agent.OnlineGreedyTabuPursuedAgent;
import cpsc470.pursuit.agent.OpenPursuedAgent;
import cpsc470.pursuit.agent.OpenPursuerAgent;
import cpsc470.pursuit.agent.PursuedAgent;
import cpsc470.pursuit.agent.PursuerAgent;
import cpsc470.pursuit.agent.SmartAStarPursuedAgent;
import cpsc470.pursuit.environment.Maze;
import cpsc470.pursuit.environment.PursuitWorldEnvironment;

public class PursuitWorldGUI implements ActionListener { // CONSIDER better to extend JFrame? (p2)

	protected enum Command {
		// Game control commands
		Reset,
		Save,

		// Automated agent control commands
		Run,
		Step,

		// Manual agent control commands
		Up,
		Down,
		Left,
		Right,
		Stay
	}
	
	protected enum AgentStrategy {
		Greedy,
		GreedyPlusTabu,
		AStar,
		SmartAStar,
		NoOp,
		Open
	}
	
	protected static class Step {
		public XYLocation pursuedAgentLocation;
		public List<XYLocation> pursuerAgentsLocations;
		
		public Step(XYLocation pursuedAgentLocation) {
			this(pursuedAgentLocation, new LinkedList<XYLocation>());
		}
		
		public Step(XYLocation pursuedAgentLocation, List<XYLocation> pursuerAgentsLocations) {
			this.pursuedAgentLocation = pursuedAgentLocation;
			this.pursuerAgentsLocations = new LinkedList<XYLocation>(pursuerAgentsLocations);
		}
		
		@Override
		public String toString() {
			StringBuffer buf = new StringBuffer();
			buf.append('(');
			buf.append(pursuedAgentLocation.getXCoOrdinate());
			buf.append(", ");
			buf.append(pursuedAgentLocation.getYCoOrdinate());
			buf.append(')');
			for (XYLocation loc : pursuerAgentsLocations) {
				buf.append('\t');
				buf.append('(');
				buf.append(loc.getXCoOrdinate());
				buf.append(", ");
				buf.append(loc.getYCoOrdinate());
				buf.append(')');
			}
			return buf.toString();
		}
	}
	
	public static final int MAX_NUM_STEPS = 150; // TODO (p2) make configurable
	public static final int NUM_MILLISECONDS_BETWEEN_STEPS = 100; //500; // TODO (p2) make configurable
	
    public static final AgentStrategy[] pursuerAgentStrategies =
    	new AgentStrategy[]{AgentStrategy.Greedy, AgentStrategy.NoOp, AgentStrategy.Open};
    public static final AgentStrategy[] pursuedAgentStrategies =
    	new AgentStrategy[]{AgentStrategy.Greedy, AgentStrategy.GreedyPlusTabu, AgentStrategy.AStar, AgentStrategy.SmartAStar, AgentStrategy.NoOp, AgentStrategy.Open};

    private JFrame frame;

    private final JFileChooser transcriptFileChooser = new JFileChooser();

    private JPanel mazePanel;
	private JPanel[][] grid;
	private JPanel pursuedAgentIcon; // CONSIDER wrap pursued agent icon implementation
	private JPanel[] pursuerAgentsIcons; // CONSIDER wrap pursuer agent icon implementation
	
	private PursuitWorldEnvironment environment;
	private XYLocation initialPursuedAgentLocation;
	private XYLocation[] initialPursuerAgentsLocations;

	private AgentStrategy pursuedAgentStrategy;
	private AgentStrategy pursuerAgentStrategy;
	
	// TODO (p2) display agent search cost statistics (time and space)
	
	private List<Step> transcript; // stores sequence of locations of agents at each step
	
	private Map<Command, JButton> buttonMap;
	private JComboBox<AgentStrategy> pursuedAgentStrategyDropdown;
	private JComboBox<AgentStrategy> pursuerAgentStrategyDropdown;

	private final Timer runTimer;

    public PursuitWorldGUI(Maze maze, XYLocation initialPursuedAgentLocation, List<XYLocation> initialPursuerAgentsLocations) {
    	
    	// Setup environment, initializing with online greedy agents.
    	this.environment = new PursuitWorldEnvironment(maze);
		PursuedAgent pursuedAgent = new OnlineGreedyPursuedAgent();
		this.initialPursuedAgentLocation = initialPursuedAgentLocation;
		environment.addPursuedAgent(pursuedAgent, this.initialPursuedAgentLocation);

		this.initialPursuerAgentsLocations = new XYLocation[initialPursuerAgentsLocations.size()];
		for (int i = 0; i < initialPursuerAgentsLocations.size(); ++i) {
			PursuerAgent pursuerAgent = new OnlineGreedyPursuerAgent();
			this.initialPursuerAgentsLocations[i] = initialPursuerAgentsLocations.get(i);
			environment.addPursuerAgent(pursuerAgent, this.initialPursuerAgentsLocations[i]);
		}
		
		this.pursuedAgentStrategy = AgentStrategy.Greedy;
		this.pursuerAgentStrategy = AgentStrategy.Greedy;
		
    	this.transcript = new LinkedList<Step>();
    	updateTranscript();
    	
    	// TODO (p2) display number of steps so far.
		
    	// Setup maze GUI.
    	int height = environment.getHeight();
    	int width = environment.getWidth();
    	this.mazePanel = new JPanel(new GridLayout(height, width));
        this.grid = new JPanel[height][width];
        for (int y = 0; y < height; ++y) {
            for(int x = 0; x < width; ++x) {
                this.grid[y][x] = new JPanel();
                this.grid[y][x].setPreferredSize(new Dimension(40, 40));
            	if (environment.isBlocked(new XYLocation(x, y))) {
            		this.grid[y][x].setBackground(Color.BLACK);
            	} else {
            		this.grid[y][x].setBackground(Color.WHITE);
            	}
            	this.grid[y][x].setBorder(BorderFactory.createDashedBorder(Color.BLACK));
                this.mazePanel.add(this.grid[y][x]);
            }
        }
        XYLocation safetyLocation = environment.getSafetyLocation();
        grid[safetyLocation.getYCoOrdinate()][safetyLocation.getXCoOrdinate()].setBackground(Color.ORANGE); // TODO (p2) use checkered or some other marking scheme

        // Setup agent icons.
        
        // TODO (p2) use better icons
        
        this.pursuedAgentIcon = new JPanel();
    	this.pursuedAgentIcon.setPreferredSize(new Dimension(20, 20));
    	this.pursuedAgentIcon.setBackground(Color.GREEN);

    	this.pursuerAgentsIcons = new JPanel[environment.getNumPursuerAgents()];
    	for (int index = 0; index < environment.getNumPursuerAgents(); ++index) {
    		this.pursuerAgentsIcons[index] = new JPanel();
	    	this.pursuerAgentsIcons[index].setPreferredSize(new Dimension(20, 20));
	    	this.pursuerAgentsIcons[index].setBackground(Color.RED);
	    	JLabel label = new JLabel(Integer.toString(index));
	    	label.setAlignmentY(JLabel.CENTER);
	    	label.setAlignmentY(JLabel.CENTER);
	    	this.pursuerAgentsIcons[index].add(label);
	    	this.pursuerAgentsIcons[index].setAlignmentX(JLabel.CENTER);
	    	this.pursuerAgentsIcons[index].setAlignmentY(JLabel.CENTER);
    	}
        
    	// Setup game controls.
    	
    	// TODO (p2) move these controls to a menu
    	
    	this.buttonMap = new HashMap<Command, JButton>();
    	
    	// TODO (p2) make buttons same size
    	
    	JPanel gameControlPanel = new JPanel();
        gameControlPanel.setLayout(new BoxLayout(gameControlPanel, BoxLayout.Y_AXIS));
        gameControlPanel.setBorder(BorderFactory.createTitledBorder("Game"));

        JButton resetButton =
        	addButton(Command.Reset.toString(), "Reset environment to initial state.", gameControlPanel, KeyEvent.VK_E);
        resetButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.buttonMap.put(Command.Reset, resetButton);
        
        JButton saveButton =
           	addButton(Command.Save.toString(), "Save game transcript to file.", gameControlPanel, KeyEvent.VK_T);
        saveButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.buttonMap.put(Command.Save, saveButton);
    	
        // TODO (p2) add open button to load new world
        
        // TODO (p2) add quit button
        
        // Setup automatic agent controls.
        JPanel automaticControlPanel = new JPanel();
        automaticControlPanel.setLayout(new BoxLayout(automaticControlPanel, BoxLayout.Y_AXIS));
        automaticControlPanel.setBorder(BorderFactory.createTitledBorder("Automatic"));
        
        JButton runButton =
        	addButton(Command.Run.toString(), "Execute remainder of pursued agent's plan.", automaticControlPanel, KeyEvent.VK_N);
        runButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        runButton.requestFocusInWindow();
        this.buttonMap.put(Command.Run, runButton);
        
        runTimer = new Timer(NUM_MILLISECONDS_BETWEEN_STEPS, null);
    	runTimer.addActionListener(getRunTimerListener());

        // TODO (p2) replace 'Run' button with 'Stop' button during run
        
        JButton stepButton =
        	addButton(Command.Step.toString(), "Execute next step in pursued agent's plan.", automaticControlPanel, KeyEvent.VK_P);
        stepButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.buttonMap.put(Command.Step, stepButton);
        
        // Setup manual pursued agent controls.
        JPanel manualControlPanel = new JPanel();
        manualControlPanel.setLayout(new BoxLayout(manualControlPanel, BoxLayout.Y_AXIS));
        manualControlPanel.setBorder(BorderFactory.createTitledBorder("Manual"));
        
        JButton upButton =
        	addButton(Command.Up.toString(), "Manually move pursued agent up.", manualControlPanel, KeyEvent.VK_U);
        upButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.buttonMap.put(Command.Up, upButton);

        JButton downButton =
        	addButton(Command.Down.toString(), "Manually move pursued agent down.", manualControlPanel, KeyEvent.VK_D);
        downButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.buttonMap.put(Command.Down, downButton);

        JButton leftButton =
        	addButton(Command.Left.toString(), "Manually move pursued agent left.", manualControlPanel, KeyEvent.VK_L);
        leftButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.buttonMap.put(Command.Left, leftButton);

        JButton rightButton =
        	addButton(Command.Right.toString(), "Manually move pursued agent up.", manualControlPanel, KeyEvent.VK_R);
        rightButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.buttonMap.put(Command.Right, rightButton);

        JButton stayButton =
        	addButton(Command.Stay.toString(), "Manually skip pursued agent's move.", manualControlPanel, KeyEvent.VK_S);
        stayButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        this.buttonMap.put(Command.Stay, stayButton);

    	// Enable arrow keys (and space for stay).
    	KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

			@Override
			public boolean dispatchKeyEvent(KeyEvent event) {
		    	if (environment.isDone() || transcript.size() >= PursuitWorldGUI.MAX_NUM_STEPS) {
		    		return false;
		    	}

				if (event.getID() == KeyEvent.KEY_PRESSED) {
	    			switch (event.getKeyCode()) {
		    			case KeyEvent.VK_UP:
		    			case KeyEvent.VK_KP_UP:
		    				processMovementControlCommand(PursuitWorldGUI.Command.Up);
		    				break;
		    				
		    			case KeyEvent.VK_DOWN:
		    			case KeyEvent.VK_KP_DOWN:
		    				processMovementControlCommand(PursuitWorldGUI.Command.Down);
		    				break;
		    				
		    			case KeyEvent.VK_LEFT:
		    			case KeyEvent.VK_KP_LEFT:
		    				processMovementControlCommand(PursuitWorldGUI.Command.Left);
		    				break;
		    			
		    			case KeyEvent.VK_RIGHT:
		    			case KeyEvent.VK_KP_RIGHT:
		    				processMovementControlCommand(PursuitWorldGUI.Command.Right);
		    				break;
		    				
		    			case KeyEvent.VK_SPACE:
		    				processMovementControlCommand(PursuitWorldGUI.Command.Stay);
		    				break;
		    				
						default:
							// no-op
					}
				}
				return false; // pass event on to next dispatcher
			}
    		
    	});

    	// Setup movement panel.
    	JPanel movementControlPanel = new JPanel();
        movementControlPanel.setLayout(new BoxLayout(movementControlPanel, BoxLayout.Y_AXIS));
        movementControlPanel.add(automaticControlPanel, BorderLayout.PAGE_START);
        movementControlPanel.add(manualControlPanel, BorderLayout.PAGE_END);
    	
        // Setup configuration controls.
        JPanel configurationControlPanel = new JPanel(new FlowLayout());
        configurationControlPanel.setBorder(BorderFactory.createTitledBorder("Configuration"));
    	
    	// CONSIDER disable these during run and/or game over?
    	
        this.pursuedAgentStrategyDropdown = new JComboBox<AgentStrategy>(PursuitWorldGUI.pursuedAgentStrategies);
        this.pursuedAgentStrategyDropdown.addActionListener(new ActionListener() {

        	public void actionPerformed(ActionEvent e) {
        		
				// TODO (p2) warn that game will be reset
				
				AgentStrategy newPursuedAgentStrategy = (AgentStrategy) pursuedAgentStrategyDropdown.getSelectedItem();
				if (newPursuedAgentStrategy == pursuedAgentStrategy) {
					return;
				}
				resetPursuedAgent(newPursuedAgentStrategy);
				
				reset();
        	}
        });
        JLabel pursuedAgentStrategyLabel = new JLabel("Pursued Agent Strategy");
        pursuedAgentStrategyLabel.setLabelFor(this.pursuedAgentStrategyDropdown);
        configurationControlPanel.add(pursuedAgentStrategyLabel);
        configurationControlPanel.add(this.pursuedAgentStrategyDropdown);

        this.pursuerAgentStrategyDropdown = new JComboBox<AgentStrategy>(PursuitWorldGUI.pursuerAgentStrategies);
        this.pursuerAgentStrategyDropdown.addActionListener(new ActionListener() {

        	public void actionPerformed(ActionEvent e) {
        		
				// TODO (p2) warn that game will be reset
				
				AgentStrategy newPursuerAgentStrategy = (AgentStrategy) pursuerAgentStrategyDropdown.getSelectedItem();
				if (newPursuerAgentStrategy == pursuerAgentStrategy) {
					return;
				}
				resetPursuerAgents(newPursuerAgentStrategy);
				
				reset();
        	}
        });
        JLabel pursuerAgentStrategyLabel = new JLabel("Pursuer Agent Strategy");
        pursuerAgentStrategyLabel.setLabelFor(this.pursuerAgentStrategyDropdown);
        configurationControlPanel.add(pursuerAgentStrategyLabel);
        configurationControlPanel.add(this.pursuerAgentStrategyDropdown);
        
        // Piece panels together.
		this.frame = new JFrame();
		Container contentPanel = this.frame.getContentPane();
		
		JPanel topPanel = new JPanel();
		topPanel.add(gameControlPanel, BorderLayout.LINE_START);
		topPanel.add(this.mazePanel, BorderLayout.CENTER);
		topPanel.add(movementControlPanel, BorderLayout.LINE_END);
		
    	contentPanel.add(topPanel, BorderLayout.PAGE_START);
    	contentPanel.add(configurationControlPanel, BorderLayout.PAGE_END);
    	
    	// Show frame.
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.pack(); //sets appropriate size for frame
        this.frame.setVisible(true);
        
        refresh(); // initialize locations of agents in gui
    }

    public void actionPerformed(ActionEvent event) {
    	PursuitWorldGUI.Command command = PursuitWorldGUI.Command.valueOf(event.getActionCommand());
    	switch (command) {
    		// Movement control events
    		case Up:
    		case Down:
    		case Left:
    		case Right:
    		case Stay:
    		case Step:
    		case Run:
    			processMovementControlCommand(command);
    			break;
    		
    		// Game control events
    		case Reset:
    		case Save:
    			processGameControlCommand(command);
    			break;
    			
    		default:
    			throw new UnsupportedOperationException("Unknown event " + event + "; debug.");
    	}
    }
    
    public void processMovementControlCommand(PursuitWorldGUI.Command command) {
    	switch (command) {

    		// Manual controls
    		case Up:
    			environment.moveObject(environment.getPursuedAgent(), XYLocation.Direction.North);
    			if (!environment.isDone()) {
        			movePursuersOneStepInEnvironment();
    			}
    			
    			// TODO (p2) if manual move matched next in pursued agent's plan, pop it off; otherwise, replan
    			
    			updateTranscript();
    			
    			refresh();
    			break;

    		case Down:
    			environment.moveObject(environment.getPursuedAgent(), XYLocation.Direction.South);
    			if (!environment.isDone()) {
        			movePursuersOneStepInEnvironment();
    			}
    			
    			// TODO (p2) if manual move matched next in pursued agent's plan, pop it off; otherwise, replan
    			
    			updateTranscript();

    			refresh();
    			break;

    		case Left:
    			environment.moveObject(environment.getPursuedAgent(), XYLocation.Direction.West);
    			if (!environment.isDone()) {
        			movePursuersOneStepInEnvironment();
    			}
    			
    			// TODO (p2) if manual move matched next in pursued agent's plan, pop it off; otherwise, replan
    			
    			updateTranscript();

    			refresh();
    			break;

    		case Right:
    			environment.moveObject(environment.getPursuedAgent(), XYLocation.Direction.East);
    			if (!environment.isDone()) {
        			movePursuersOneStepInEnvironment();
    			}
    			
    			// TODO (p2) if manual move matched next in pursued agent's plan, pop it off; otherwise, replan
    			
    			updateTranscript();

    			refresh();
    			break;

    		case Stay:
    			movePursuersOneStepInEnvironment();
    			
    			// TODO (p2) if manual move matched next in pursued agent's plan, pop it off; otherwise, replan
    			
    			updateTranscript();

    			refresh();
    			break;

    		// Automatic controls
    		case Run:
    			disableMovementControls();
    			runTimer.start();
    			
    			// TODO (p2) disable other controls while run is executing
    			
	    		break;
	    		
    		case Step:
    			executeOneStepInEnvironment();
    			refresh();
        		break;
    			
    		default:
    			throw new UnsupportedOperationException("Unknown movement control " + command + "; debug.");
    	}
    	
    	if (environment.isDone() || transcript.size() >= PursuitWorldGUI.MAX_NUM_STEPS) {
    		endGame();
    	}
    }
    
    public void processGameControlCommand(PursuitWorldGUI.Command command) {
    	switch (command) {

			case Reset:
				if (runTimer.isRunning()) {
					runTimer.stop();
				}
				int option =
					JOptionPane.showConfirmDialog(
						frame,
						"WARNING: You are about to reset the game. Are you sure?",
						"Reset Warning",
						JOptionPane.OK_CANCEL_OPTION);
				
				// TODO (p2) make CANCEL the default
				
		    	if (option != JOptionPane.CANCEL_OPTION) {
					reset();
		    	} else {
		    		runTimer.restart();
		    	}
		    	
				break;
	        	
    		case Save:
    			saveTranscript();
    			break;
    			
        	default:
    			throw new UnsupportedOperationException("Unknown game control command " + command + "; debug.");
    	}
    }
    
    protected ActionListener getRunTimerListener() {
    	return new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
    			if (!environment.isDone() && transcript.size() < PursuitWorldGUI.MAX_NUM_STEPS) {
    				
    				// TODO (p2) also check that game hasn't been reset (and/or have reset stop the timer)!
    				
    				executeOneStepInEnvironment();
    				refresh();
    			} else {
    				runTimer.stop();
    				endGame();
    			}
			}
			
		};
    }
    
    protected void updateTranscript() {
		Step step = new Step(environment.getCurrentLocationFor(environment.getPursuedAgent()));
		for (int i = 0; i < environment.getNumPursuerAgents(); ++i) {
			step.pursuerAgentsLocations.add(environment.getCurrentLocationFor(environment.getPursuerAgent(i)));
		}
		transcript.add(step);
    }

    /**
     * Save transcript to file selected by user.
     */
	protected void saveTranscript() {
		int fileChoiceReturnVal = transcriptFileChooser.showSaveDialog(frame);
		if (fileChoiceReturnVal == JFileChooser.APPROVE_OPTION) {
			Path transcriptFile = transcriptFileChooser.getSelectedFile().toPath();
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(Files.newBufferedWriter(transcriptFile, Charset.defaultCharset()));
				writer.println("Pursued agent strategy: " + pursuedAgentStrategy);
				writer.println("Pursuer agent strategy: " + pursuerAgentStrategy);
				
				writer.println("Pursued agent metrics: " + environment.getPursuedAgent().getInstrumentation());
				for (int i = 0; i < environment.getNumPursuerAgents(); ++i) {
					writer.println("Pursuer agent " + i + " metrics: " + environment.getPursuerAgent(i).getInstrumentation());
				}
				
				writer.println("Transcript:");
				writer.print("Pursued");
				for (int i = 0; i < environment.getNumPursuerAgents(); ++i) {
					writer.print("\tPursuer" + i);
				}
				writer.println();
				for (Step step : transcript) {
					writer.println(step);
				}
				
				// CONSIDER transcript should also record move made by each agent at each step
				
				// TODO (p2) transcript should also record which pursued agent steps were manual
				
				JOptionPane.showMessageDialog(
					frame,
					"Transcript saved to " + transcriptFile + ".",
					"Transcript saved",
					JOptionPane.INFORMATION_MESSAGE);
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(
					frame,
					"Save to transcript file '" + transcriptFile + "' failed.\n" + ioe.getMessage(),
					"Transcript save failure",
					JOptionPane.ERROR_MESSAGE);
			} finally {
				if (writer != null) {
					writer.close();
				}
			}
		}
	}
    
    /**
     * Update GUI to match current environment state. In particular, place each agent in its current location.
  	 * Note: If pursuer and pursued are co-located, draw pursuer on top iff it is in safe location.
     */
    protected void refresh() {
    	
    	boolean isPursuedAgentSafe = environment.isPursuedAgentSafe();
    	if (isPursuedAgentSafe) {
    		// Pursued agent is safe -- draw it on top if it shares location with any pursuers.
	    	XYLocation pursuedAgentLocation = environment.getCurrentLocationFor(environment.getPursuedAgent());
	    	moveAgentIcon(pursuedAgentIcon, pursuedAgentLocation);
    	}
    	for (int index = 0; index < environment.getNumPursuerAgents(); ++index) {
        	XYLocation pursuerAgentLocation = environment.getCurrentLocationFor(environment.getPursuerAgent(index));
	    	moveAgentIcon(pursuerAgentsIcons[index], pursuerAgentLocation);
    	}
    	if (!isPursuedAgentSafe) {
    		// Pursued agent is not safe -- draw any pursuers that share its location on top.
	    	XYLocation pursuedAgentLocation = environment.getCurrentLocationFor(environment.getPursuedAgent());
	    	moveAgentIcon(pursuedAgentIcon, pursuedAgentLocation);
    	}
    	
    	mazePanel.repaint();
    }

    /**
     * Show game over message and disable movement controls.
     * @throw RuntimeException If game isn't really over.
     */
    protected void endGame() {
    	if (environment.isPursuedAgentSafe()) {
			JOptionPane.showMessageDialog(
					frame,
					"GAME OVER! Pursued agent is SAFE!",
					"Game Over",
					JOptionPane.INFORMATION_MESSAGE);
    	} else if (environment.isPursuedAgentCaught()) {
			JOptionPane.showMessageDialog(
					frame,
					"GAME OVER! Pursued agent is CAUGHT!",
					"Game Over",
					JOptionPane.INFORMATION_MESSAGE);
    	} else if (transcript.size() >= MAX_NUM_STEPS) {
			JOptionPane.showMessageDialog(
					frame,
					"GAME OVER! Maximum number of steps (" + MAX_NUM_STEPS + ") exceeded!",
					"Game Over",
					JOptionPane.INFORMATION_MESSAGE);
    	} else {
    		throw new RuntimeException("Unexpected game end; please debug!");
    	}
    
    	disableMovementControls();
    }
    
    protected void disableMovementControls() {
    	buttonMap.get(Command.Run).setEnabled(false);
    	buttonMap.get(Command.Step).setEnabled(false);
    	
    	buttonMap.get(Command.Up).setEnabled(false);
    	buttonMap.get(Command.Down).setEnabled(false);
    	buttonMap.get(Command.Left).setEnabled(false);
    	buttonMap.get(Command.Right).setEnabled(false);
    	buttonMap.get(Command.Stay).setEnabled(false);
    	
    	refresh();
    }

    protected void enableMovementControls() {
    	buttonMap.get(Command.Run).setEnabled(true);
    	buttonMap.get(Command.Step).setEnabled(true);
    	
    	buttonMap.get(Command.Up).setEnabled(true);
    	buttonMap.get(Command.Down).setEnabled(true);
    	buttonMap.get(Command.Left).setEnabled(true);
    	buttonMap.get(Command.Right).setEnabled(true);
    	buttonMap.get(Command.Stay).setEnabled(true);
    	
    	refresh();
    }
    
    /**
     * Reset agents' locations and internals, clear transcript, and re-enable movement controls if disabled.
     * 
     * Note: Resetting agent internals currently implemented by creating new instantiation of agent.
     */
    protected void reset() {

    	// Reset agents' locations and internals to initial state.
    	resetPursuedAgent(pursuedAgentStrategy);
    	environment.moveObjectToAbsoluteLocation(environment.getPursuedAgent(), initialPursuedAgentLocation);

    	resetPursuerAgents(pursuerAgentStrategy);
    	for (int i = 0; i < initialPursuerAgentsLocations.length; ++i) {
    		environment.moveObjectToAbsoluteLocation(environment.getPursuerAgent(i), initialPursuerAgentsLocations[i]);
    	}
    	
    	// Clear history.
    	transcript.clear();

    	// Re-enable movement controls.
    	enableMovementControls();
    }

    protected void resetPursuedAgent(AgentStrategy newAgentStrategy) {
		PursuedAgent newPursuedAgent;
		switch (newAgentStrategy) {
			case Greedy:
				newPursuedAgent = new OnlineGreedyPursuedAgent();
				break;
				
			case GreedyPlusTabu:
				newPursuedAgent = new OnlineGreedyTabuPursuedAgent();
				break;
				
			case AStar:
				newPursuedAgent = new AStarPursuedAgent();
				break;
				
			case SmartAStar:
				newPursuedAgent = new SmartAStarPursuedAgent();
				break;
				
			case NoOp:
				newPursuedAgent = new NoOpPursuedAgent();
				break;
				
			case Open:
				newPursuedAgent = new OpenPursuedAgent();
				break;
				
			default:
				throw new UnsupportedOperationException("Unexpected pursuer agent strategy '" + newAgentStrategy + "'; debug.");
		}
		
		environment.replacePursuedAgent(newPursuedAgent);

		pursuedAgentStrategy = newAgentStrategy;
    }

    protected void resetPursuerAgents(AgentStrategy newAgentStrategy) {
		for (int i = 0; i < environment.getNumPursuerAgents(); ++i) {
			PursuerAgent newPursuerAgent;
			switch (newAgentStrategy) {
				case Greedy:
					newPursuerAgent = new OnlineGreedyPursuerAgent();
					break;
					
				case NoOp:
					newPursuerAgent = new NoOpPursuerAgent();
					break;
					
				case Open:
					newPursuerAgent = new OpenPursuerAgent();
					break;
					
				default:
					throw new UnsupportedOperationException("Unexpected pursued agent strategy '" + newAgentStrategy + "'; debug.");
			}
			
			environment.replacePursuerAgent(i, newPursuerAgent);
		}

		pursuerAgentStrategy = newAgentStrategy;
    }

    /**
     * Moves pursued agent one step according to its program then, if game isn't done,
     * also moves all pursuer agents one step according to their programs.
     */
    protected void executeOneStepInEnvironment() {
		PursuedAgent pursuedAgent = environment.getPursuedAgent();
		Percept percept = environment.getPerceptSeenBy(pursuedAgent);
		Action pursuedAction = pursuedAgent.execute(percept);
		environment.executeAction(pursuedAgent, pursuedAction);
		
		if (!environment.isDone()) {
			movePursuersOneStepInEnvironment(); 
		}
		
		updateTranscript();
    }
    
    /**
     * Moves pursuers one step each.
     * Note: Assumes game isn't done.
     * Note: All pursuers move.
     */
    protected void movePursuersOneStepInEnvironment() {
		for (int index = 0; index < environment.getNumPursuerAgents(); ++index) {
			PursuerAgent pursuerAgent = environment.getPursuerAgent(index);
			Percept percept = environment.getPerceptSeenBy(pursuerAgent);
			Action pursuerAction = pursuerAgent.execute(percept);
			environment.executeAction(pursuerAgent, pursuerAction);
		}
    }
    
    protected JButton addButton(String name, String tip, Container container) {
    	return addButton(name, tip, container, null);
    }

    protected JButton addButton(String name, String tip, Container container, Integer keyEvent) {
        JButton button = new JButton(name);
        button.setToolTipText(tip);
        button.setActionCommand(name);
        if (keyEvent != null) {
            button.setMnemonic(keyEvent);
        }
        button.addActionListener(this);
        container.add(button);
        
        return button;
    }
    
    protected void moveAgentIcon(JPanel agent, XYLocation newLocation) {
    	JPanel cell = grid[newLocation.getYCoOrdinate()][newLocation.getXCoOrdinate()];
    	cell.add(agent);
    }
}
