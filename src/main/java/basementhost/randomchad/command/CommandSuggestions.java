package basementhost.randomchad.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;

import java.util.Collection;

// 让多个指令共用同一套 Tab 补全提示。
@FunctionalInterface
public interface CommandSuggestions {

	Collection<String> suggest(CommandSourceStack source, String[] args);
}
