package org.rockhopper.smarthome.wes.jwes.communicator;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpClient {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private String host;
    private FTPClient ftp;

    private boolean logged;

    public FtpClient(String host) {
        this.host = host;
        ftp = new FTPClient();
    }

    public boolean login(String username, String password) {
        FTPClientConfig config = new FTPClientConfig(FTPClientConfig.SYST_NT);
        ftp.configure(config);
        try {
            int reply;
            ftp.connect(host);
            logger.info("(FTP) Connected to {}.", host);
            logger.debug(ftp.getReplyString());

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                disconnect(true);
            } else {
                ftp.enterLocalPassiveMode();
                if (ftp.login(username, password)) {
                    logged = true;
                }
            }

        } catch (IOException e) {
            logger.error("Exception caught while FTP login", e);
            disconnect(false);
        }
        return logged;
    }

    public void logout() {
        if (logged) {
            try {
                ftp.logout();
            } catch (IOException e) {
                // nothing more we can do
            } finally {
                disconnect(false);
            }
        }
    }

    public String printWorkingDirectory() throws IOException {
        if (logged) {
            return ftp.printWorkingDirectory();
        }
        return null;
    }

    public boolean changeWorkingDirectory(String pathname) throws IOException {
        if (logged) {
            if (pathname != null) {
                return ftp.changeWorkingDirectory(pathname);
            }
        }
        return false;
    }

    public boolean removeDirectory(String pathname) throws IOException {
        if (logged) {
            if (pathname != null) {
                return ftp.removeDirectory(pathname);
            }
        }
        return false;
    }

    public void deleteFiles(boolean force) throws IOException {
        String workingDir = printWorkingDirectory();
        if (workingDir == null) {
            return;
        }
        if (!workingDir.endsWith("/")) {
            workingDir += "/";
        }
        if (!force) {
            // check current directory
            if ("/".equals(workingDir)) {
                throw new IllegalArgumentException(
                        "FtpClient#deleteFiles(boolean false): Cannot delete all files from root folder without flag 'force' set!");
            }
        }
        FTPFile[] ftpFiles = listFiles();
        if ((ftpFiles != null) && (ftpFiles.length > 0)) {
            for (FTPFile ftpFile : ftpFiles) {
                if (ftpFile != null) {
                    String pathname = workingDir + ftpFile.getName();
                    logger.debug("Deleting: [{}]", pathname);
                    ftp.deleteFile(pathname);
                }
            }
        }
    }

    public FTPFile[] listFiles() throws IOException {
        if (logged) {
            return ftp.listFiles(null, new FTPFileFilter() {
                @Override
                public boolean accept(FTPFile file) {
                    return file != null && file.isFile();
                }
            });
        }
        return null;
    }

    public boolean makeDirectory(String pathname) throws IOException {
        if (logged) {
            if (pathname != null) {
                return ftp.makeDirectory(pathname);
            }
        }
        return false;
    }

    public boolean uploadClassPathFile(String remote, String classpathLocal) throws IOException {
        if (logged) {
            if (remote != null) {
                try (InputStream local = getClass().getClassLoader().getResourceAsStream(classpathLocal)) {
                    return ftp.storeFile(remote, local);
                }
            }
        }
        return false;
    }

    public boolean uploadFile(String remote, InputStream local) throws IOException {
        if (logged) {
            if (remote != null) {
                return ftp.storeFile(remote, local);
            }
        }
        return false;
    }

    private boolean disconnect(boolean forceDisconnect) {
        boolean error = false;
        if (forceDisconnect || ftp.isConnected()) {
            try {
                ftp.disconnect();
            } catch (IOException ioe) {
                // nothing more we can do
                error = true;
            }
        }
        return !error;
    }

}
