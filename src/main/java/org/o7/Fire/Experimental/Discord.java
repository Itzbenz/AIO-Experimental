package org.o7.Fire.Experimental;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import org.o7.Fire.MachineLearning.Jenetic.RawBasicNeuralNet;
import org.o7.Fire.MachineLearning.Jenetic.ReactorTestJenetic;
import org.o7.Fire.MachineLearning.Reinforcement.ReactorControl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class Discord {
	final static ReactionEmoji.Unicode upvote = ReactionEmoji.unicode("\u2B06"), downvote = ReactionEmoji.unicode("\u2B07"), update = ReactionEmoji.unicode("\u25B6"), reset = ReactionEmoji.unicode("\uD83D\uDD04"), AI = ReactionEmoji.unicode("\uD83E\uDDEC");
	static volatile Snowflake messageID = null;
	static File model = new File("ReactorTestJenetic-NeuralNetwork-Best.json");
	static Gson gson = new GsonBuilder().setPrettyPrinting().create();
	static RawBasicNeuralNet neuralNet;
	
	static {
		reloadNet();
	}
	
	public static void reloadNet() {
		try {
			neuralNet = gson.fromJson(new FileReader(model), RawBasicNeuralNet.class);
		}catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		ReactorControl.welcomes();
		final String token = args[0];
		final DiscordClient client = DiscordClient.create(token);
		final GatewayDiscordClient gateway = client.login().block();
		gateway.on(ReactionAddEvent.class).subscribe(event -> {
			if (messageID == null) return;
			if (event.getUser().block().isBot()) return;
			event.getMessage().subscribe(m -> {
				if (!m.getId().equals(messageID)) return;
				Message channel = m;
				System.out.println(event.getEmoji());
				m.removeAllReactions().block();
				ReactionEmoji emoji = event.getEmoji();
				if (upvote.equals(emoji)) {
					ReactorControl.raiseControlRod();
				}else if (downvote.equals(emoji)) {
					ReactorControl.lowerControlRod();
				}else if (reset.equals(emoji)) {
					ReactorControl.reactor.reset();
					ReactorControl.welcome();
				}else if (AI.equals(emoji)) {
					double[] output = neuralNet.process(ReactorControl.reactor.factor());
					boolean doSomething = false;
					if (output[0] > 0.5f) {
						doSomething = true;
						ReactorControl.reactor.raiseControlRod();
					}
					if (output[1] > 0.5f) {
						doSomething = true;
						ReactorControl.reactor.lowerControlRod();
					}
					if (!doSomething) ReactorControl.log.accept("AI DO NOTHING AT ALL");
				}
				ReactorControl.update();
				if (ReactorControl.reactor.reactorFuckingExploded()) {
					ReactorControl.update();
					channel.addReaction(reset).block();
				}else {
					channel.addReaction(upvote).subscribe();
					channel.addReaction(downvote).subscribe();
					channel.addReaction(update).block();
					channel.addReaction(AI).block();
				}
			});
		});
		gateway.on(MessageCreateEvent.class).subscribe(event -> {
			final Message message = event.getMessage();
			if (message.getAuthor().isEmpty()) return;
			System.out.println(message.getAuthor().get().getTag());
			System.out.println(message.getContent());
			String s = message.getContent();
			if (s.startsWith("!setup")) {
				Message channel = message.getChannel().block().createMessage(ReactorControl.welcomes()).block();
				ReactorControl.log = as -> channel.edit(m -> m.setContent(as)).subscribe();
				messageID = channel.getId();
				channel.addReaction(upvote).subscribe();
				channel.addReaction(downvote).subscribe();
				channel.addReaction(update).block();
				channel.addReaction(AI).block();
			}else if (s.startsWith("test")) {
				message.getChannel().block().createMessage("assad").subscribe();
			}else if (s.startsWith("!eval")) {
				message.getChannel().subscribe(m -> m.createMessage("Giving Test Exam To AI").subscribe(am -> am.edit(c -> c.setContent("Fitness: " + ReactorTestJenetic.eval(neuralNet))).subscribe()));
			}else if (s.startsWith("!reload")) {
				message.getChannel().subscribe(m -> m.createMessage("Reloading Model").subscribe(am -> am.edit(c -> {
					reloadNet();
					c.setContent("Reloaded Network Fitness: " + ReactorTestJenetic.eval(neuralNet));
				}).subscribe()));
				
			}
			
			
		});
		
		gateway.onDisconnect().block();
	}
}
