package org.jamsim.ascape;

import java.util.Map;

import net.casper.io.beans.util.JodaBeanUtil;
import net.casper.io.beans.util.PropertyException;

import org.apache.commons.collections.map.CaseInsensitiveMap;
import org.apache.commons.lang.NotImplementedException;
import org.ascape.model.Cell;
import org.joda.property.Bean;
import org.joda.property.Property;
import org.joda.util.IdentityBasedHashMap;

/**
 * Implements Joda Properties on an Ascape cell. Sub-classes will want to
 * override {@link #initPropertyMap()}.
 * 
 * @author Oliver Mannion
 * @version $Revision$
 */
public class BeanCell extends Cell implements Bean, Bean.Internal {

	/**
	 * Serialization ID.
	 */
	private static final long serialVersionUID = -7962411999932991558L;

	/**
	 * The map of properties.
	 */
	private Map iProperties = null;

	@Override
	public Object cloneDeep() {
		throw new NotImplementedException();
	}

	@Override
	public Bean cloneDeep(IdentityBasedHashMap clonedObjects) {
		throw new NotImplementedException();
	}

	@Override
	public Class getBeanType() {
		return getClass();
	}

	@Override
	public Property getProperty(String propertyName) {
		return (org.joda.property.Property) getPropertyMap()
				.get(propertyName);
	}

	@Override
	public Map getPropertyMap() {
		if (iProperties == null) {
			iProperties = new CaseInsensitiveMap(20);
			initPropertyMap();
		}
		return iProperties;
	}

	/**
	 * Initialize the map of properties. Subclasses can override the default
	 * reflection behaviour by calling back to the addProperty methods.
	 */
	protected void initPropertyMap() {
		// for overriding by subclasses to add their
		// own hard or soft wired properties
	}

	@Override
	public String getAttribute(String key) {
		throw new NotImplementedException();
	}

	@Override
	public int getAttributeCount() {
		throw new NotImplementedException();
	}

	@Override
	public Map getAttributeMap() {
		throw new NotImplementedException();
	}

	@Override
	public void setAttribute(String key, String value) {
		throw new NotImplementedException();
	}

	public Property addVirtualProperty(String name, Class<?> basicType)
			throws PropertyException {
		return JodaBeanUtil.addVirtualProperty(this, name, basicType);
	}

	public org.joda.property.Property addSoftWiredProperty(String name) {
		return JodaBeanUtil.addSoftWiredProperty(this, name);
	}

	public void addHardWiredProperty(String name) {
		JodaBeanUtil.addHardWiredProperty(this, name);
	}
}