package bot;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.request.*;

import java.util.ArrayList;
import java.util.List;

public class Bot {
    private final TelegramBot bot = new TelegramBot(System.getenv ("BOT_TOKEN"));
    private final String PROCESSING_LABEL = "Processing...";
    public static final List<String> opponentWins = new ArrayList<String>() {{
        add("01");
        add("12");
        add("20");
    }};


    public void serve() {

        bot.setUpdatesListener(updates -> {
            updates.forEach(this::process);
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }

    private void process(Update update) {
        Message message =  update.message();
        CallbackQuery callbackQuery = update.callbackQuery();
        InlineQuery inlineQuery = update.inlineQuery();

        BaseRequest request = null;

        if (message != null && message.viaBot() != null && message.viaBot().username().equals("RSP_UpV_bot")) {
            InlineKeyboardMarkup replyMarkup = message.replyMarkup();
            if (replyMarkup == null) {
                return;
            }
            InlineKeyboardButton[][] buttons = replyMarkup.inlineKeyboard();
            if  (buttons == null) {
                return;
            }
            InlineKeyboardButton button = buttons[0][0];
            String buttonLabel = button.text();

            if (!buttonLabel.equals(PROCESSING_LABEL)) {
                return;
            }

            Long chatId = message.chat().id();
            String senderName = message.from().firstName();
            String senderChose = button.callbackData();
            Integer messageId = message.messageId();

            request = new EditMessageText(chatId, messageId, message.text())
                    .replyMarkup(
                            new InlineKeyboardMarkup(
                                    new InlineKeyboardButton("\uD83E\uDDB7")
                                            .callbackData(String.format("%d %s %s %s", chatId, senderName, senderChose, "0")),
                                    new InlineKeyboardButton("✂️")
                                            .callbackData(String.format("%d %s %s %s", chatId, senderName, senderChose, "1")),
                                    new InlineKeyboardButton("\uD83D\uDCDC")
                                            .callbackData(String.format("%d %s %s %s", chatId, senderName, senderChose, "2"))
                            )
                    );

        } else if (inlineQuery != null) {
            InlineQueryResultArticle rook = buildInLineButton("rook", "\uD83E\uDDB7 Камень", "0");
            InlineQueryResultArticle scissors = buildInLineButton("scissors", "✂️ Ножницы", "1");
            InlineQueryResultArticle paper = buildInLineButton("paper", "\uD83D\uDCDC Бумага", "2");

            request = new AnswerInlineQuery(inlineQuery.id(), rook, scissors, paper).cacheTime(1);
        } else if (callbackQuery != null) {
            String[] data = callbackQuery.data().split(" ");
            Long chatId = Long.parseLong(data[0]);
            String senderName = data[1];
            String senderChose = data[2];
            String opponentChose = data[3];
            String opponentName = callbackQuery.from().firstName();

            if (senderChose.equals(opponentChose)) {
                request = new SendMessage(
                        chatId,
                        String.format(
                                "Между %s (%s) и %s (%s) ничья",
                                senderName, senderChose,
                                opponentName, opponentChose
                        )
                );
            } else if (opponentWins.contains(senderChose + opponentChose)) {
                request = new SendMessage(
                        chatId,
                        String.format(
                                "%s (%s) выиграл %s (%s)",
                                senderName, senderChose,
                                opponentName, opponentChose
                        )
                );
            } else  {
                request = new SendMessage(
                        chatId,
                        String.format(
                                "%s (%s) выиграл %s (%s)",
                                opponentName, opponentChose,
                                senderName, senderChose
                        ));
            }
        }
//         else if (message != null) {
//            long chatId = message.chat().id();
//            request = new SendMessage(chatId,"Hello");
//        }

        if (request != null) {
            bot.execute(request);
        }

    }

    private InlineQueryResultArticle buildInLineButton(String id, String title, String callBackData) {
        return new InlineQueryResultArticle(id, title, "I'm ready!")
                .replyMarkup(
                        new InlineKeyboardMarkup(
                                new InlineKeyboardButton(PROCESSING_LABEL).callbackData(callBackData)
                        )
                );
    }
}