package com.aws.codestar.projecttemplates;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class Controller {

    Map<String, CardInfo> vmap = new TreeMap<>();
    Controller() {
        CardInfo card1 = new CardInfo("444433333", "VISA", "PREMIUM CREDIT", "abhi", "my_visa_premium");
        vmap.put("444433333", card1);
        CardInfo card2 = new CardInfo("512176622", "MC", "GOLD CREDIT", "abhi", "my_mc_gold");
        vmap.put("512176622", card2);
    }

    @RequestMapping("/cardtype/{cardnumber}")
    public CardType handleCardType(@PathVariable("cardnumber") String cardnumberinput) {
        String cardnumber = cardnumberinput.replaceAll("\\s",""); // strip white space

        if (cardnumber.length() < 6) { // if too short
            return new CardType("Unknown", "Unknown");
        }

        try {
            return GetCardType(cardnumber);
        } catch (NumberFormatException ex) {
            return new CardType("Unknown", "Unknown"); // if non-digit
        }
    }

    // create a record for a card
    @RequestMapping(value = "/cards/", method = RequestMethod.POST)
    public @ResponseBody boolean handleCreate(@RequestBody Map<String, Object> payload) {
        if (!payload.containsKey("cardnumber")) {
            return false;
        }

        String cardnumber = payload.get("cardnumber").toString();

        // now we have cardnumber, type, subtype, store them together with optional nickname, userid into db.

        String cardholder = payload.containsKey("cardholder") ? payload.get("cardholder").toString() : null;
        String nickname = payload.containsKey("nickname") ? payload.get("nickname").toString() : null;

        addCard(cardnumber, cardholder, nickname);

        return true;
    }

    private void addCard(String cardnumber, String cardholder, String nickname) {
        CardType ctype = GetCardType(cardnumber);
        CardInfo info = new CardInfo(cardnumber, ctype.getType(), ctype.getSubtype(), cardholder, nickname);
        vmap.put(cardnumber, info);
    }

    // delete credit card record
    @RequestMapping(value = "/cards/{cardnumber}", method = RequestMethod.DELETE)
    public @ResponseBody boolean handleDelete(@PathVariable("cardnumber") String cardnumber) {
        //System.out.println(cardnumber);
        // Go to db ....
        if (vmap.containsKey(cardnumber)) {
            vmap.remove(cardnumber);
            return true;
        }

        return false;
    }


    // update credit card record
    @RequestMapping(value = "/cards/{cardnumber}", method = RequestMethod.PUT)
    public @ResponseBody boolean handleUpdate(@PathVariable("cardnumber") String cardnumber, @RequestBody Map<String, Object> payload) {
        // Go to db ....
        if (!vmap.containsKey(cardnumber) || !payload.containsKey("cardnumber")) return false; // both old and new cardnumbers should be available
        String newcardnumber = payload.get("cardnumber").toString();
        if (!cardnumber.equals(newcardnumber) && vmap.containsKey(newcardnumber)) return false; // duplicate card number

        vmap.remove(cardnumber);


        String cardholder = payload.containsKey("cardholder") ? payload.get("cardholder").toString() : null;
        String nickname = payload.containsKey("nickname") ? payload.get("nickname").toString() : null;

        addCard(newcardnumber, cardholder, nickname);
        return true;
    }

    @RequestMapping(value = "/cards/", method = RequestMethod.GET)
    public @ResponseBody
    List<CardInfo> handleGet() {
        // if cardnumer not null, get via cardnumber directly
        // else use hodername + nick name
        // else return wrong
        // Go to db ....
        List<CardInfo> rslt = new ArrayList<>();
        for (Map.Entry<String, CardInfo> entry : vmap.entrySet()) {
            rslt.add(entry.getValue());
        }

        return rslt;
    }


    CardType GetCardType(String cardNumber) {
        //int digit = Integer.parseInt(cardNumber);
        long digit = Long.parseLong(cardNumber);
        if (cardNumber.length() >= 9) {
            int first9digit = Integer.parseInt(cardNumber.substring(0, 9));
            if ((444433333 <= first9digit) && (first9digit <= 444532332)) return new CardType("VISA", "PREMIUM CREDIT");
            else if ((512176622 <= first9digit) && (first9digit <= 512189239)) return new CardType("MC", "GOLD CREDIT");
            else if ((546626193 <= first9digit) && (first9digit <= 546691237)) return new CardType("MC", "BUSINESS");
        }

        int first6digit = Integer.parseInt(cardNumber.substring(0, 6));
        if (first6digit == 455561) return new CardType("VISA", "DEBIT");
        else if (first6digit == 387765) return new CardType("AMEX", "CREDIT");
        else if (first6digit == 454545) return new CardType("VISA", "CREDIT");
        else if (first6digit == 546626) return new CardType("MC", "CREDIT");
        return new CardType("Unknown", "Unknown");
    }

}
