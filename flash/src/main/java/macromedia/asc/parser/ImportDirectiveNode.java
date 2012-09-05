/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package macromedia.asc.parser;

import macromedia.asc.semantics.*;
import macromedia.asc.util.*;

/**
 * Node
 *
 * @author Jeff Dyer
 */
public class ImportDirectiveNode extends DefinitionNode
{
	public AttributeListNode attrs;
	public PackageNameNode name;
	public PackageDefinitionNode pkg_node;
	public ReferenceValue ref;
	public boolean package_retrieved;
	public Context cx;

	public ImportDirectiveNode(PackageDefinitionNode pkgdef, AttributeListNode attrs, PackageNameNode name, PackageDefinitionNode pkg_node, Context cx)
	{
		super(pkgdef, attrs, -1);
		ref = null;
		this.attrs = attrs;
		this.name = name;
		this.pkg_node = pkg_node;
		package_retrieved = false;
		this.cx = cx;
	}

	public Value evaluate(Context cx, Evaluator evaluator)
	{
		if (evaluator.checkFeature(cx, this))
		{
			return evaluator.evaluate(cx, this);
		}
		else
		{
			return null;
		}
	}

	public boolean isConst()
	{
		return true;
	}

	public Node initializerStatement(Context cx)
	{
		return cx.getNodeFactory().emptyStatement();
	}

	public String toString()
	{
		return "importdirective";
	}
}
