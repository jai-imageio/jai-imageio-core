package com.github.jaiimageio.impl.common;


import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

/**
 * Created on 06/01/17.
 *
 * @author Reda.Housni-Alaoui
 */
public class ClassLoaderUtilsTest {

	private ClassLoaderUtils classLoaderUtils = ClassLoaderUtils.getInstance();

	@Test
	public void given_object_foo_when_calling_wasLoadedByCurrentClassLoader_on_foo_then_it_should_return_true() throws Exception {
		MockObject foo = new MockObject();
		assertTrue(classLoaderUtils.wasLoadedByCurrentClassLoaderAncestor(foo));
	}

	@Test
	public void given_object_foo_when_calling_wasLoadedByCurrentClassLoader_on_foo_from_thread_on_child_classloader_then_it_should_return_true() throws Exception {
		URLClassLoader clMain = (URLClassLoader) Thread.currentThread().getContextClassLoader();

		Object foo = Class.forName(MockObject.class.getName(), true, clMain).newInstance();
		assertSame(clMain, foo.getClass().getClassLoader());

		ClassLoader clA = new ChildFirstClassLoader(clMain.getURLs(), clMain);

		Thread.currentThread().setContextClassLoader(clA);
		try {
			assertTrue(classLoaderUtils.wasLoadedByCurrentClassLoaderAncestor(foo));
		} finally {
			Thread.currentThread().setContextClassLoader(clMain);
		}
	}

	@Test
	public void given_object_foo_created_by_cl_B_when_calling_wasLoadedByCurrentClassLoader_on_foo_from_thread_on_cl_A_then_it_should_return_false() throws Exception {
		URLClassLoader clMain = (URLClassLoader) Thread.currentThread().getContextClassLoader();

		ClassLoader clA = new ChildFirstClassLoader(clMain.getURLs(), clMain);
		ClassLoader clB = new ChildFirstClassLoader(clMain.getURLs(), clMain);

		Object foo = Class.forName(MockObject.class.getName(), true, clB).newInstance();
		assertSame(clB, foo.getClass().getClassLoader());

		Thread.currentThread().setContextClassLoader(clA);
		try {
			assertFalse(classLoaderUtils.wasLoadedByCurrentClassLoaderAncestor(foo));
		} finally {
			Thread.currentThread().setContextClassLoader(clMain);
		}
	}

	public static class ChildFirstClassLoader extends URLClassLoader {
		private ClassLoader system;

		public ChildFirstClassLoader(URL[] classpath, ClassLoader parent) {
			super(classpath, parent);
			system = getSystemClassLoader();
		}

		@Override
		protected synchronized Class<?> loadClass(String name, boolean resolve)
				throws ClassNotFoundException {
			// First, check if the class has already been loaded
			Class<?> c = findLoadedClass(name);
			if (c == null) {
				try {
					// checking local
					c = findClass(name);
				} catch (ClassNotFoundException e) {
					c = loadClassFromParent(name, resolve);
				} catch(SecurityException e){
					c = loadClassFromParent(name, resolve);
				}
			}
			if (resolve)
				resolveClass(c);
			return c;
		}

		private Class<?> loadClassFromParent(String name, boolean resolve) throws ClassNotFoundException {
			// checking parent
			// This call to loadClass may eventually call findClass
			// again, in case the parent doesn't find anything.
			Class<?> c;
			try {
				c = super.loadClass(name, resolve);
			} catch (ClassNotFoundException e) {
				c = loadClassFromSystem(name);
			} catch (SecurityException e){
				c = loadClassFromSystem(name);
			}
			return c;
		}

		private Class<?> loadClassFromSystem(String name) throws ClassNotFoundException{
			Class<?> c = null;
			if (system != null) {
				// checking system: jvm classes, endorsed, cmd classpath,
				// etc.
				c = system.loadClass(name);
			}
			return c;
		}

		@Override
		public URL getResource(String name) {
			URL url = findResource(name);
			if (url == null)
				url = super.getResource(name);

			if (url == null && system != null)
				url = system.getResource(name);

			return url;
		}

		@Override
		public Enumeration<URL> getResources(String name) throws IOException {
			/**
			 * Similar to super, but local resources are enumerated before parent
			 * resources
			 */
			Enumeration<URL> systemUrls = null;
			if (system != null) {
				systemUrls = system.getResources(name);
			}
			Enumeration<URL> localUrls = findResources(name);
			Enumeration<URL> parentUrls = null;
			if (getParent() != null) {
				parentUrls = getParent().getResources(name);
			}
			final List<URL> urls = new ArrayList<URL>();
			if (localUrls != null) {
				while (localUrls.hasMoreElements()) {
					URL local = localUrls.nextElement();
					urls.add(local);
				}
			}
			if (systemUrls != null) {
				while (systemUrls.hasMoreElements()) {
					urls.add(systemUrls.nextElement());
				}
			}
			if (parentUrls != null) {
				while (parentUrls.hasMoreElements()) {
					urls.add(parentUrls.nextElement());
				}
			}
			return new Enumeration<URL>() {
				Iterator<URL> iter = urls.iterator();

				public boolean hasMoreElements() {
					return iter.hasNext();
				}

				public URL nextElement() {
					return iter.next();
				}
			};
		}

		public InputStream getResourceAsStream(String name) {
			URL url = getResource(name);
			try {
				return url != null ? url.openStream() : null;
			} catch (IOException e) {
			}
			return null;
		}
	}

}