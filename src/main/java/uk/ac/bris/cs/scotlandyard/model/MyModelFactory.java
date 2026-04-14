package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import com.google.common.collect.ImmutableSet;
import jakarta.annotation.Nonnull;

import org.checkerframework.checker.nullness.qual.NonNull;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.ArrayList;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		return new MyModel(setup, mrX, detectives);
	}
	private final class MyModel implements Model{
			GameSetup setup;
			Player mrX;
			ImmutableList<Player> detectives;
			ArrayList<Board.GameState> gameStates;
			ArrayList<Observer> observers;

			private MyModel(
					GameSetup setup,
					Player mrX,
					ImmutableList<Player> detectives
			) {
				this.setup = setup;
				this.mrX = mrX;
				this.detectives = detectives;
				this.observers = new ArrayList<>();
				this.gameStates = new ArrayList<>();
				MyGameStateFactory firstGameState = new MyGameStateFactory();
				this.gameStates.add(firstGameState.build(setup, mrX, detectives));
			}

		@Override
		public @NonNull Board getCurrentBoard() {
				return gameStates.get(gameStates.size()-1);
		}

		@Override
		public void registerObserver(Model.Observer observer) {
				if (observer == null) { throw new NullPointerException("Null observer can't be registered"); }
				if (this.observers.contains(observer)) { throw new IllegalArgumentException("Observer already exists"); }
				this.observers.add(observer);
		}

		@Override
		public void unregisterObserver(Model.Observer observer) {
			if (observer == null) { throw new NullPointerException("Trying to remove a null observer"); }
			if (!this.observers.contains(observer)) { throw new IllegalArgumentException("Observer to unregister does not exist."); }
			this.observers.remove(observer);
			}

		@Override
		public @NonNull ImmutableSet<Observer> getObservers() {
				return ImmutableSet.copyOf(this.observers);
		}

		@Override
		public void chooseMove(@NonNull Move move) {
			Board.GameState lastState = gameStates.get(gameStates.size()-1);
			Board.GameState newState = lastState.advance(move);
			gameStates.add(newState);
			if (newState.getWinner().isEmpty()) {
				for (Observer o : observers) {
					o.onModelChanged(newState, Observer.Event.MOVE_MADE);
				}
			} else {
				for (Observer o : observers) {
					o.onModelChanged(newState, Observer.Event.GAME_OVER);
				}
			}
		}
	}
}
