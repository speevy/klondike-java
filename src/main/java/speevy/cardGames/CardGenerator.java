package speevy.cardGames;

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import lombok.Value;

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
		Document baseCard = getResourceAsDocument("card");
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

	private void setCardValue(Document doc, Card card) throws XPathException, ParserConfigurationException, SAXException, IOException {
		String symbol = card.suit().getSymbol();
		String cardValue = card.rank().getName() + card.suit().getSymbol();
		Node topText = getNodeById(doc, "topText");
		topText.appendChild(doc.createTextNode(cardValue));
		
		Element g = (Element) xPath.evaluate("/svg/g", doc, XPathConstants.NODE);
		List<PositionAndSize> positions = positionMap.get(card.rank());
		
		if (positions == null) {
			//J, Q, K
			Document iconDocument = getResourceAsDocument(card.rank().getName());
			
			Element icon = (Element) xPath.evaluate("/svg/g", iconDocument, XPathConstants.NODE);
			
			icon.setAttribute("transform", "translate(0, 25) scale(2)");
			icon.setAttribute("class", "icon");
			
			g.appendChild(doc.importNode(icon, true));
			
		} else {
			positions.forEach(pos -> {
				g.appendChild(drawMain(doc, pos, symbol));
			});
		}
	}

	private Element drawMain(Document doc, PositionAndSize pos, String content) {
		Element text = doc.createElement("text");
		text.setAttribute("x", Double.toString(pos.x));
		text.setAttribute("y", Double.toString(pos.y));
		text.setAttribute("font-size", Double.toString(pos.size) + "pt");
		text.setAttribute("class", "main");
		text.appendChild(doc.createTextNode(content));
		return text;
	}
	
	private Node getNodeById(Node doc, String id) throws XPathException {
		
		return (Node)xPath.evaluate("//*[@id=\""+id+"\"]", doc, XPathConstants.NODE);
	}

	private Document getResourceAsDocument(String name) throws ParserConfigurationException, SAXException, IOException {
		
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
 
        return docBuilder.parse(CardGenerator.class.getResourceAsStream("/" + name + ".svg"));
	}


    private void saveCard(Document doc, String path) throws IOException, TransformerException {
        DOMSource source = new DOMSource(doc);

        FileWriter writer = new FileWriter(new File(path));
        StreamResult result = new StreamResult(writer);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.transform(source, result);
    }
    
    @Value
    private class PositionAndSize {
    	private final double x;
    	private final double y;
    	private final double size;
    }
    
    private final Map<CardRank, List<PositionAndSize>> positionMap = Map.of(
    		AmericanCardRank.ACE, List.of(
    				new PositionAndSize(30, 55, 25)
    				),
    		AmericanCardRank.TWO, List.of(
    				new PositionAndSize(30, 30, 15),
    				new PositionAndSize(30, 75, 15)
    				),
    		AmericanCardRank.THREE, List.of(
    				new PositionAndSize(30, 35, 15),
    				new PositionAndSize(30, 55, 15),
    				new PositionAndSize(30, 75, 15)
    				),
    		AmericanCardRank.FOUR, List.of(
    				new PositionAndSize(15, 35, 15),
    				new PositionAndSize(15, 75, 15),
    				new PositionAndSize(45, 35, 15),
    				new PositionAndSize(45, 75, 15)
    				),
    		AmericanCardRank.FIVE, List.of(
    				new PositionAndSize(15, 35, 15),
    				new PositionAndSize(15, 75, 15),
    				new PositionAndSize(45, 35, 15),
    				new PositionAndSize(45, 75, 15),
    				new PositionAndSize(30, 55, 15)
   				),
    		AmericanCardRank.SIX, List.of(
    				new PositionAndSize(15, 35, 15),
    				new PositionAndSize(15, 75, 15),
    				new PositionAndSize(45, 35, 15),
    				new PositionAndSize(45, 75, 15),
    				new PositionAndSize(15, 55, 15),
    				new PositionAndSize(45, 55, 15)
    				),
    		AmericanCardRank.SEVEN, List.of(
    				new PositionAndSize(15, 35, 15),
    				new PositionAndSize(15, 75, 15),
    				new PositionAndSize(45, 35, 15),
    				new PositionAndSize(45, 75, 15),
    				new PositionAndSize(15, 55, 15),
    				new PositionAndSize(45, 55, 15),
    				new PositionAndSize(30, 45, 15)
    				),
    		AmericanCardRank.EIGHT, List.of(
    				new PositionAndSize(15, 35, 15),
    				new PositionAndSize(15, 75, 15),
    				new PositionAndSize(45, 35, 15),
    				new PositionAndSize(45, 75, 15),
    				new PositionAndSize(15, 55, 15),
    				new PositionAndSize(45, 55, 15),
    				new PositionAndSize(30, 45, 15),
    				new PositionAndSize(30, 65, 15)
    				),
    		AmericanCardRank.NINE, List.of(
    				new PositionAndSize(15, 35, 10),
    				new PositionAndSize(15, 48.33, 10),
    				new PositionAndSize(15, 61.66, 10),
    				new PositionAndSize(15, 75, 10),
    				new PositionAndSize(45, 35, 10),
    				new PositionAndSize(45, 48.33, 10),
    				new PositionAndSize(45, 61.66, 10),
    				new PositionAndSize(45, 75, 10),
    				new PositionAndSize(30, 55, 10)
    				),
    		AmericanCardRank.TEN, List.of(
    				new PositionAndSize(15, 35, 10),
    				new PositionAndSize(15, 48.33, 10),
    				new PositionAndSize(15, 61.66, 10),
    				new PositionAndSize(15, 75, 10),
    				new PositionAndSize(45, 35, 10),
    				new PositionAndSize(45, 48.33, 10),
    				new PositionAndSize(45, 61.66, 10),
    				new PositionAndSize(45, 75, 10),
    				new PositionAndSize(30, 41.66, 10),
    				new PositionAndSize(30, 68.33, 10)
    				)
    		);
    
    
}
