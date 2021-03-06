/**
 *  Copyright 2011, Tobias Senger
 *  
 *  This file is part of animamea.
 *
 *  Animamea is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Animamea is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License   
 *  along with animamea.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.tsenger.animamea.iso7816;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.smartcardio.CommandAPDU;

import org.bouncycastle.asn1.ASN1Encoding;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERTaggedObject;

import de.tsenger.animamea.asn1.CertificateHolderAuthorizationTemplate;

/**
 * Die Klasse MSESetAT dient zur Konstruktions einer "MSE:Set id_AT"-APDU
 * 
 * @author Tobias Senger (tobias@t-senger.de)
 * 
 */
public class MSESetAT {

	public static final int setAT_PACE = 1;
	public static final int setAT_CA = 2;
	public static final int setAT_TA = 3;

	public static final int KeyReference_MRZ = 1;
	public static final int KeyReference_CAN = 2;
	public static final int KeyReference_PIN = 3;
	public static final int KeyReference_PUK = 4;

	private final byte CLASS = (byte) 0x00;
	private final byte INS = (byte) 0x22; // Instruction Byte: Message Security
											// Environment
	private byte P1=0;
	private final byte P2=(byte)0xA4;
	private byte[] do80CMR = null;
	private byte[] do83KeyReference = null;
	private byte[] do83KeyName = null;
	private byte[] do84PrivateKeyReference = null;
	private byte[] do7F4C_CHAT = null;
	private byte[] do91EphemeralPublicKEy = null;

	public MSESetAT() {}

	/**
	 * Setzt das zu verwendende Authentication Template (id_PACE, id_CA oder id_TA)
	 * 
	 * @param at
	 *            {@link de.tsenger.androsmex.pace.MSECommand.setAT_PACE},
	 *            {@link de.tsenger.androsmex.pace.MSECommand.setAT_CA},
	 *            {@link de.tsenger.androsmex.pace.MSECommand.setAT_TA}
	 */
	public void setAT(int at) {
		if (at == setAT_PACE) P1 = (byte) 0xC1;			
		if (at == setAT_CA)	P1 = (byte) 0x41;
		if (at == setAT_TA)	P1 = (byte) 0x81;
	}

	/**
	 * Setzt die OID des zu verwendenden Protokolls
	 * 
	 * @param protocol
	 *            Das zu verwendende Protokoll
	 */
	public void setProtocol(String protocol) {
		ASN1ObjectIdentifier oid = new ASN1ObjectIdentifier(protocol);
		DERTaggedObject to = new DERTaggedObject(false, 0x00, oid);
		try {
			do80CMR = to.getEncoded(ASN1Encoding.DER);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Setzt das Tag 0x83 (Reference of public / secret key) f??r id_PACE
	 * 
	 * @param kr
	 *            Referenziert das verwendete Passwort: 1: MRZ 2: CAN 3: PIN 4:
	 *            PUK
	 */
	public void setKeyReference(int kr) {
		DERTaggedObject to = new DERTaggedObject(false, 0x03, new ASN1Integer(kr));
		try {
			do83KeyReference = to.getEncoded(ASN1Encoding.DER);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Setzt das Tag 0x83 (Reference of public / secret key) f??r Terminal
	 * Authentication
	 * 
	 * @param kr
	 *            String der den Namen des Public Keys des Terminals beinhaltet
	 *            (ISO 8859-1 kodiert)
	 */
	public void setKeyReference(String kr) {
		DERTaggedObject to = new DERTaggedObject(false, 0x03, new DEROctetString(kr.getBytes()));
		try {
			do83KeyName = to.getEncoded(ASN1Encoding.DER);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Setzt das Tag 0x84 (Reference of a private key / Reference for computing
	 * a session key)
	 * 
	 * @param pkr
	 *            Bei id_PACE wird der Index der zu verwendenden Domain Parameter
	 *            angegeben Bei id_CA wird der Index des zu verwendenden Private
	 *            Keys angegeben Bei id_RI wird der Index des zu verwendenden
	 *            Private Keys angegeben
	 */
	public void setPrivateKeyReference(int pkr) {
		DERTaggedObject to = new DERTaggedObject(false, 0x04, new ASN1Integer(pkr));
		try {
			do84PrivateKeyReference = to.getEncoded(ASN1Encoding.DER);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void setAuxiliaryAuthenticatedData() throws UnsupportedOperationException {
		// TODO noch zu implementieren, Tag 0x67
		throw new UnsupportedOperationException("setAuxiliaryAuthenticationData not yet implemented!");
	}

	/**
	 * Setzt das Tag 0x91 (Ephemeral Public Key). Der PK muss bereits komprimiert 
	 * (siehe comp()-Funktion in TR-03110) sein.
	 * @param pubKey comp(ephemeral PK_PCD) -> TR-03110 A.2.2.3
	 */
	public void setEphemeralPublicKey(byte[] pubKey) {
		DERTaggedObject to = new DERTaggedObject(false, 0x11, new DEROctetString(pubKey));
		try {
			do91EphemeralPublicKEy = to.getEncoded(ASN1Encoding.DER);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param chat
	 */
	public void setCHAT(CertificateHolderAuthorizationTemplate chat) {
		try {
			do7F4C_CHAT = chat.getEncoded(ASN1Encoding.DER);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Konstruiert aus den gesetzten Objekten eine MSE-Command-APDU 
	 * @return ByteArray mit MSE:SetAT APDU
	 */
	public CommandAPDU getCommandAPDU() {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		if (do80CMR != null)
			bos.write(do80CMR, 0, do80CMR.length);
		if (do83KeyReference != null)
			bos.write(do83KeyReference, 0, do83KeyReference.length);
		if (do83KeyName != null)
			bos.write(do83KeyName, 0, do83KeyName.length);
		if (do84PrivateKeyReference != null)
			bos.write(do84PrivateKeyReference, 0, do84PrivateKeyReference.length);
		if (do91EphemeralPublicKEy != null) 
			bos.write(do91EphemeralPublicKEy, 0 , do91EphemeralPublicKEy.length);
		if (do7F4C_CHAT != null)
			bos.write(do7F4C_CHAT, 0, do7F4C_CHAT.length);
		byte[] data = bos.toByteArray();

		return new CommandAPDU(CLASS, INS, P1, P2, data);
	}

}
