/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package copydamagedfiles;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import static java.nio.file.FileVisitResult.CONTINUE;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author administrador
 */
public class CopyDamagedFiles {

    public void copy(final Path source, final Path target, final FileAlreadyExistsAction action) {
        try {
//            DirectoryStream<Path> newDirectoryStream = Files.newDirectoryStream(source);
//            for (Path path : newDirectoryStream) {
//                System.out.println("path = " + path);
//            }
            Path walkFileTree = Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    System.out.println("Error copiando" + file + " = " + exc);
                    return CONTINUE; //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    Path targetdir = target.resolve(source.relativize(dir));
                    try {
                        Files.copy(dir, targetdir, StandardCopyOption.COPY_ATTRIBUTES);
                    } catch (FileAlreadyExistsException e) {
                        if (!Files.isDirectory(targetdir)) {
                            Logger.getLogger(CopyDamagedFiles.class.getName()).log(Level.SEVERE, "Es carpeta: " + Boolean.toString(Files.isDirectory(targetdir)), e);
//                            throw e;
                        }
                    } catch (IOException ex) {
                        Logger.getLogger(CopyDamagedFiles.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    Path resolve = target.resolve(source.relativize(file));
                    try {

                        try {
                            System.out.println("copiando" + file);
                            Files.copy(file, resolve);
                        } catch (FileAlreadyExistsException e) {
                            System.out.println("    FileAlreadyExists");
                            switch (action) {
                                case REEMPLAZAR:
                                    System.out.println("    se reemplazo");
                                    Files.copy(file, resolve, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                                    break;
                                case REEMPLAZAR_SI_ES_DIFERENTE:
                                    BasicFileAttributes bfaF = Files.readAttributes(file, BasicFileAttributes.class);
                                    FileTime cF = bfaF.creationTime();
                                    FileTime mF = bfaF.lastModifiedTime();
                                    BasicFileAttributes bfaR = Files.readAttributes(resolve, BasicFileAttributes.class);
                                    FileTime cR = bfaR.creationTime();
                                    FileTime mR = bfaR.lastModifiedTime();
                                    int CT = cF.compareTo(cR);
                                    int MT = mF.compareTo(mR);
                                    if (CT != 0 && MT != 0) {
                                        System.out.println("    se reemplazo porque es diferente");
                                        System.out.println("    creationTime file " + cF + ":: creationTime resolve " + cR + "::" + CT);
                                        System.out.println("    modifiedTime file " + mF + ":: modifiedTime resolve " + mR + "::" + MT);
                                        try {
                                            Files.copy(file, resolve, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES);
                                        } catch (DirectoryNotEmptyException ex) {
                                            Logger.getLogger(CopyDamagedFiles.class.getName()).log(Level.SEVERE, "Es archivo: " + Boolean.toString(Files.isDirectory(file)), ex);
                                        }
                                    } else {
                                        System.out.println("    no se copio porque es el igual");
                                    }
                                    break;
                                case OMITIR:
                                    System.out.println("    se omitio");
                                    break;
                                case CAMBIAR_NOMBRE_AL_COPIADO:

                                    break;
                                default:
                                    throw new AssertionError();
                            }

                        }
                    } catch (IOException e) {
                        Logger.getLogger(CopyDamagedFiles.class.getName()).log(Level.SEVERE, null, e);
                    }
                    return CONTINUE;
                }
            }
            );
            System.out.println(
                    "walkFileTree = " + walkFileTree);
        } catch (IOException ex) {
            Logger.getLogger(CopyDamagedFiles.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
//        final Path source = Paths.get("/media/administrador/TOSHIBA/Fabiel/Tecnologia");
//        final Path target = Paths.get("/home/administrador/Escritorio/Nuevo");
        final Path source = Paths.get("/media/administrador/TOSHIBA/Fabiel/Mis Documentos/Biblioteca de Musica");
        final Path target = Paths.get("/media/administrador/eaf2e557-4466-4698-8721-0d73899c9681/Fabiel/Biblioteca de Musica");
        CopyDamagedFiles cdf = new CopyDamagedFiles();
        cdf.copy(source, target, FileAlreadyExistsAction.REEMPLAZAR_SI_ES_DIFERENTE);
    }

}
