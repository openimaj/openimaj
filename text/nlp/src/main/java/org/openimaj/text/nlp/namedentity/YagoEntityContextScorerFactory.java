/**
 * Copyright (c) 2011, The University of Southampton and the individual contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   * 	Redistributions of source code must retain the above copyright notice,
 * 	this list of conditions and the following disclaimer.
 *
 *   *	Redistributions in binary form must reproduce the above copyright notice,
 * 	this list of conditions and the following disclaimer in the documentation
 * 	and/or other materials provided with the distribution.
 *
 *   *	Neither the name of the University of Southampton nor the names of its
 * 	contributors may be used to endorse or promote products derived from this
 * 	software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.openimaj.text.nlp.namedentity;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.openimaj.text.nlp.namedentity.NamedEntity.Type;

/**
 * Factory Object for building {@link YagoEntityContextScorer}
 * 
 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 */
public class YagoEntityContextScorerFactory {
	
	/**
	 * Create from a lucene index file.
	 * 
	 * @param indexPath
	 * @return {@link YagoEntityContextScorer}
	 */
	public static YagoEntityContextScorer createFromIndexFile(String indexPath)
			{
		YagoEntityContextScorer yci = new YagoEntityContextScorer();
		File f = new File(indexPath);
		if (f.isDirectory()) {
			try {
				yci.index = new SimpleFSDirectory(f);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else
			return null;
		return yci;
	}

	/**
	 * Class that uses an underlying lucene index to match tokens to companies.
	 * Use the enclosing factory class to instantiate.
	 * 
	 * @author Laurence Willmore (lgw1e10@ecs.soton.ac.uk)
	 * 
	 */
	public static class YagoEntityContextScorer extends
			EntityContextScorer<List<String>, NamedEntity> {

		private Directory index = null;
		/**
		 * lucene index field names.
		 */
		public final String[] names = { "uri", "context", "type" };
		private FieldType[] types;
		private IgnoreTokenStripper ss;
		private QuickSearcher qs;

		private YagoEntityContextScorer() {
			FieldType ti = new FieldType();
			ti.setIndexed(true);
			ti.setTokenized(true);
			ti.setStored(true);
			FieldType n = new FieldType();
			n.setStored(true);
			n.setIndexed(true);
			types = new FieldType[3];
			types[0] = n;
			types[1] = ti;
			ss = new IgnoreTokenStripper(IgnoreTokenStripper.Language.English);
			qs = null;
		}

		@Override
		public HashMap<NamedEntity, Float> getScoredEntitiesFromContext(
				List<String> context) {
			if (qs == null)
				instantiateQS();
			String contextString = StringUtils.join(
					ss.getNonStopWords(context), " ");
			try {
				// search on the context field
				String[] retFields = new String[] { names[0], names[2] };
				HashMap<String[], Float> searchresults = qs.search(names[1],
						retFields, contextString, 1);
				HashMap<NamedEntity, Float> results = new HashMap<NamedEntity, Float>();
				for (String[] srv : searchresults.keySet()) {
					NamedEntity yne = new NamedEntity(srv[0],
							Enum.valueOf(Type.class, srv[1]));
					results.put(yne, searchresults.get(srv));
				}
				return results;

			} catch (ParseException e) {

				e.printStackTrace();
			} catch (IOException e) {

				e.printStackTrace();
			}
			return null;
		}

		@Override
		public Map<NamedEntity, Float> getScoresForEntityList(
				List<String> entityUris, List<String> context) {
			if (qs == null)
				instantiateQS();
			String contextString = StringUtils.join(
					ss.getNonStopWords(context), " ");
			if (entityUris.size() > 0) {
				String[] retFields = new String[] { names[0], names[2] };
				HashMap<String[], Float> searchresults= qs.searchFiltered(names[1], retFields, contextString,
						names[0], entityUris);
				HashMap<NamedEntity, Float> results = new HashMap<NamedEntity, Float>();
				for (String[] srv : searchresults.keySet()) {
					NamedEntity yne = new NamedEntity(srv[0],
							Enum.valueOf(Type.class, srv[1]));
					results.put(yne, searchresults.get(srv));
				}
				return results;				
			} else
				return new HashMap<NamedEntity, Float>();
		}
		
		@Override
		public Map<NamedEntity, Float> getScoresForEntityList(
				List<String> entityUris, String context) {
			if (qs == null)
				instantiateQS();
			if (entityUris.size() > 0) {
				String[] retFields = new String[] { names[0], names[2] };
				HashMap<String[], Float> searchresults= qs.searchFiltered(names[1], retFields, context,
						names[0], entityUris);
				HashMap<NamedEntity, Float> results = new HashMap<NamedEntity, Float>();
				for (String[] srv : searchresults.keySet()) {
					NamedEntity yne = new NamedEntity(srv[0],
							Enum.valueOf(Type.class, srv[1]));
					results.put(yne, searchresults.get(srv));
				}
				return results;				
			} else
				return new HashMap<NamedEntity, Float>();
		}

		private void instantiateQS() {
			qs = new QuickSearcher(index, new StandardAnalyzer(
					Version.LUCENE_40));
		}

	}

}
