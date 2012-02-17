/**
 * Copyright  2006-2010 Soyatec
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 *
 * $Id$
 */
package org.soyatec.windowsazure.authenticate;

/**
 * 
 * Represents a signed identifier for shared access url.
 * 
 * @author xiaowei.ye@soyatec.com
 * 
 */
public interface ISignedIdentifier {

	/**
	 * @return the policy
	 */
	public abstract IAccessPolicy getPolicy();

	/**
	 * @param policy
	 *            the policy to set
	 */
	public abstract void setPolicy(IAccessPolicy policy);

	/**
	 * @return the id
	 */
	public abstract String getId();

	/**
	 * @param id
	 *            the id to set
	 */
	public abstract void setId(String id);

}