package com.flatironschool.javacs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import org.jsoup.select.Elements;

public class WikiPhilosophy {
	
	final static WikiFetcher wf = new WikiFetcher();
	static int num_parens = 0;
	
	/**
	 * Tests a conjecture about Wikipedia and Philosophy.
	 * 
	 * https://en.wikipedia.org/wiki/Wikipedia:Getting_to_Philosophy
	 * 
	 * 1. Clicking on the first non-parenthesized, non-italicized link
     * 2. Ignoring external links, links to the current page, or red links
     * 3. Stopping when reaching "Philosophy", a page with no links or a page
     *    that does not exist, or when a loop occurs
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String url =  getFirstValidLinkOnPage("https://en.wikipedia.org/wiki/Java_(programming_language)");
		while (!url.equals("https://en.wikipedia.org/wiki/Philosophy")){
			if (url.equals("")) return;
			System.out.println(url);
			url = getFirstValidLinkOnPage(url);
		}
		System.out.println(url);
		return;
	}

	public static String getFirstValidLinkOnPage(String url) throws IOException {
		//reset parenthesis count
		num_parens = 0;
		Elements paragraphs = wf.fetchWikipedia(url);

		// for each paragraph on the page
		for (Element paragraph : paragraphs) {
			// iterate through the dom tree
			Iterable<Node> iter = new WikiNodeIterable(paragraph);
			for (Node node: iter) {
				if (node instanceof TextNode) {
					// update num_parenthesis
					for (char character : ((TextNode) node).text().toCharArray()) {
						if (character == '(') num_parens ++;
						else if (character == ')') num_parens --;
					}

					if (isValidLink((TextNode) node)) {
						String linked_url = ((Element)node.parent()).attr("abs:href");
						// if this link is not the current link, return it!
						if (!linked_url.equals(url) && !linked_url.contains(url + "#")) return linked_url;
					}
				}
	        }
	    }
	    System.out.println("Returning a nil");
  		return "";
	}

	/** 
	 *	Given a text node, returns true if it is a link that is not 
	 * 	in italics, parenthesis, or links to the current page.
	**/

	public static boolean isValidLink(TextNode node) {
		Element parent = (Element) node.parent();
		return (parent.tagName() == "a" && num_parens <= 0 && !isItalicized(node));
	}

	public static boolean isItalicized(TextNode node) {
		Element parent = (Element) node.parent();
		if (parent.tagName() == "i" || parent.tagName() == "em") return true;
		for (Element ancestor : parent.parents()) {
			if (ancestor.tagName() == "i" || ancestor.tagName() == "em") return true;
		}
		return false;
	}
}