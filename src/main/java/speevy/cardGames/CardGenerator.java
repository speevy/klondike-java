package speevy.cardGames;

import java.io.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class CardGenerator {


	public static void main(String[] args) throws Exception {
		new CardGenerator().generateCards();
	}
	
	private final XPath xPath = XPathFactory.newInstance().newXPath();
	
	private void generateCards() {
		new AmericanCards().getAll().forEach(this::generateCard);
	}

	private void generateCard(final Card card) {
		try {
		Document baseCard = getBaseCard();
		setCardValue(baseCard, card);
		setCardColor(baseCard, card);
		saveCard(baseCard, getFileName(card));
		} catch (RuntimeException | ParserConfigurationException | SAXException | IOException | XPathException | TransformerException e) {
			throw new RuntimeException("Error generating card : " + card, e);
		}
	}

	private String getFileName(Card card) {
		final String suit = card.suit().getName().toLowerCase();
		final String rank;
		switch (card.rank().getName()) {
		case "A": rank = "1"; break;
		case "J": rank = "jack"; break;
		case "Q": rank = "queen"; break;
		case "K": rank = "king"; break;
		default: rank = card.rank().getName();
		}
		return "card_" + rank + "_" + suit.substring(0, suit.length() - 1) + ".svg";
	}

	private void setCardColor(Document document, Card card) throws DOMException, XPathException {
		Element style = document.createElement("style");
		final String color = card.suit().getGroupName().toLowerCase();
		style.appendChild(document.createTextNode(".cardColor {fill: " + color + "}"));
		((Node)xPath.evaluate("svg/defs", document, XPathConstants.NODE)).appendChild(style);
		NodeList stops = document.getElementsByTagName("stop");
		for (int i = 0; i < stops.getLength(); i++) {
			((Element)stops.item(i)).setAttribute("stop-color", color);
		}
	}

	private void setCardValue(Document baseCard, Card card) throws XPathException {
		String cardValue = card.rank().getName() + card.suit().getSymbol();
		Node topText = getNodeById(baseCard, "topText");
		topText.appendChild(baseCard.createTextNode(cardValue));
		
		Node mainText = getNodeById(baseCard, "mainText");
		mainText.appendChild(baseCard.createTextNode(card.suit().getSymbol()));
	}

	private Node getNodeById(Node doc, String id) throws XPathException {
		
		return (Node)xPath.evaluate("//*[@id=\""+id+"\"]", doc, XPathConstants.NODE);
	}

	private Document getBaseCard() throws ParserConfigurationException, SAXException, IOException {
		
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        // root elements

        return docBuilder.parse(CardGenerator.class.getResourceAsStream("/card.svg"));
	}


    private void saveCard(Document doc, String path) throws IOException, TransformerException {
        DOMSource source = new DOMSource(doc);

        FileWriter writer = new FileWriter(new File(path));
        StreamResult result = new StreamResult(writer);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(source, result);
    }
}
