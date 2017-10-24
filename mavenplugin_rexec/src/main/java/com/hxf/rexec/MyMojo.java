package com.hxf.rexec;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.sftp.RemoteResourceInfo;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.PublicKey;
import java.util.List;

import static org.bouncycastle.crypto.tls.ConnectionEnd.client;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch
 * @phase process-sources
 */
public class MyMojo
        extends AbstractMojo {
    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;


    /**
     * 表达式需要加目标前缀可以用<configure>user</configure>来配置
     * @parameter expression="${touch.user}"
     */
    private String user;

    /**
     * @parameter expression="${touch.passwd}"
     */
    private String passwd;

    /**
     * @parameter @parameter expression="${touch.path}"
     */
    private String path;


    public void execute()
            throws MojoExecutionException {

        SSHClient ssh = new SSHClient();
        Session session = null;
        try {
            ssh.addHostKeyVerifier(
                    new HostKeyVerifier() {

                        public boolean verify(String s, int i, PublicKey publicKey) {
                            return true;
                        }
                    });
            ssh.connect("localhost");
            ssh.authPassword(user, passwd);
            session = ssh.startSession();
            session.allocateDefaultPTY();
            Session.Shell shell = session.startShell();

            SFTPClient sftp = ssh.newSFTPClient();
            List<RemoteResourceInfo> files = sftp.ls(path);
            for (RemoteResourceInfo file : files) {
                System.out.println(file.getPath());
            }

            sftp.close();


            session.close();
            ssh.close();


        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //=========================
        File f = outputDirectory;

        if (!f.exists()) {
            f.mkdirs();
        }

        File touch = new File(f, "touch.txt");

        FileWriter w = null;
        try {
            w = new FileWriter(touch);

            w.write("touch.txt");
        } catch (IOException e) {
            throw new MojoExecutionException("Error creating file " + touch, e);
        } finally {
            if (w != null) {
                try {
                    w.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

//
//
//
//    private void upload() throws Exception {
//        System.out.println("Uploading");
//        final SSHClient ssh = new SSHClient();
//        ssh.loadKnownHosts();
//        String password = config.password;
//        if (password == null) {
//            System.out.println("Enter password");
//            final Scanner s = new Scanner(System.in);
//            password = s.nextLine();
//            if (MiscUtils.isBlank(password)) {
//                throw new RuntimeException("Invalid password: blank");
//            }
//        }
//        System.out.println("Connecting");
//        ssh.connect("rt.sk");
//        try {
//            System.out.println("Authenticating");
//            ssh.authPassword("moto", password);
//            System.out.println("Uploading version");
//            final String targetFName = REMOTE_DIR + "/" + config.getTargetFileName();
//            exec(ssh, "echo `date +%Y%m%d` >" + REMOTE_DIR + "/" + config.getTargetFileName() + ".version");
//            exec(ssh, "rm -f " + targetFName);
//            System.out.println("Uploading");
//            final SCPFileTransfer ft = ssh.newSCPFileTransfer();
//            ft.upload(config.getTargetFileName(), targetFName);
//        } finally {
//            ssh.disconnect();
//        }
//    }
//
//
//    private SSHClient connectToDatastore(Datastore datastore) throws IOException {
//
//        final SSHClient ssh = new SSHClient();
//        ssh.loadKnownHosts();
//        // Accept all hosts
//        ssh.addHostKeyVerifier(
//                new HostKeyVerifier() {
//                    public boolean verify(String arg0, int arg1, PublicKey arg2) {
//                        return true; // don't bother verifying
//                    }
//                });
//
//        ssh.connect(datastore.getIp(), datastore.getPort());
//        ssh.authPublickey(datastore.getLoginId());
//
//        //ssh.authPublickey(datastore.getLoginId(), privateKey.getAbsolutePath());
//
//        return ssh;
//    }
}
